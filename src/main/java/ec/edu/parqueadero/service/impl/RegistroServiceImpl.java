package ec.edu.parqueadero.service.impl;

import ec.edu.parqueadero.aspect.Auditable;
import ec.edu.parqueadero.dto.*;
import ec.edu.parqueadero.entity.*;
import ec.edu.parqueadero.exception.*;
import ec.edu.parqueadero.repository.*;
import ec.edu.parqueadero.service.NotificacionService;
import ec.edu.parqueadero.service.RegistroService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegistroServiceImpl implements RegistroService {

    /** Minutos de gracia sin costo (RN-05). */
    public static final long MINUTOS_TOLERANCIA = 10;

    private final RegistroEstacionamientoRepository registroRepository;
    private final VehiculoRepository vehiculoRepository;
    private final EspacioRepository espacioRepository;
    private final NotificacionService notificacionService;
    private final Clock clock;

    @Override
    @Transactional
    @Auditable(operacion = "REGISTRAR_INGRESO")
    public RegistroResponse registrarIngreso(IngresoRequest req) {
        String placa = VehiculoServiceImpl.normalizarPlaca(req.placa());
        Vehiculo vehiculo = vehiculoRepository.findByPlacaIgnoreCase(placa)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vehiculo", "placa", placa));

        // RN-04: el propietario debe estar activo
        if (!Boolean.TRUE.equals(vehiculo.getUsuario().getActivo()))
            throw new ReglaNegocioException("USUARIO_INACTIVO",
                    "El propietario del vehiculo " + placa + " esta inactivo y no puede ingresar");

        // RN-02: un vehiculo no puede tener dos ingresos activos
        if (registroRepository.existsByVehiculoIdAndEstado(vehiculo.getId(), EstadoRegistro.ACTIVO))
            throw new ReglaNegocioException("INGRESO_DUPLICADO",
                    "El vehiculo " + placa + " ya se encuentra dentro del parqueadero");

        // RN-03: espacio disponible y compatible con el tipo de vehiculo
        Espacio espacio = req.espacioId() == null ? asignarAutomaticamente(vehiculo.getTipo())
                : validarEspacioSolicitado(req.espacioId(), vehiculo.getTipo());

        espacio.setEstado(EstadoEspacio.OCUPADO);
        RegistroEstacionamiento r = RegistroEstacionamiento.builder()
                .vehiculo(vehiculo).espacio(espacio)
                .fechaIngreso(LocalDateTime.now(clock))
                .estado(EstadoRegistro.ACTIVO)
                .observacion(req.observacion())
                .build();
        return DtoMapper.toResponse(registroRepository.save(r));
    }

    @Override
    @Transactional
    @Auditable(operacion = "REGISTRAR_SALIDA")
    public RegistroResponse registrarSalida(Long registroId, boolean notificacionAsincrona) {
        RegistroEstacionamiento r = registroRepository.buscarDetalle(registroId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Registro", "id", registroId));
        if (r.getEstado() != EstadoRegistro.ACTIVO)
            throw new ReglaNegocioException("SALIDA_YA_REGISTRADA",
                    "El registro " + registroId + " ya tiene salida registrada el " + r.getFechaSalida());

        LocalDateTime salida = LocalDateTime.now(clock);
        long minutos = Math.max(0, Duration.between(r.getFechaIngreso(), salida).toMinutes());
        r.setFechaSalida(salida);
        r.setMinutosPermanencia(minutos);
        r.setMontoCobrado(calcularMonto(minutos, r.getEspacio().getTarifaHora()));
        r.setEstado(EstadoRegistro.FINALIZADO);
        r.getEspacio().setEstado(EstadoEspacio.DISPONIBLE);

        RegistroResponse respuesta = DtoMapper.toResponse(registroRepository.save(r));
        if (notificacionAsincrona) notificacionService.enviarComprobanteAsync(respuesta);
        else notificacionService.enviarComprobante(respuesta);
        return respuesta;
    }

    @Override
    public RegistroResponse obtener(Long id) {
        return registroRepository.buscarDetalle(id).map(DtoMapper::toResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("Registro", "id", id));
    }

    @Override
    public List<RegistroResponse> listarActivos() {
        return registroRepository.buscarActivos().stream().map(DtoMapper::toResponse).toList();
    }

    @Override
    public List<RegistroResponse> historialPorPlaca(String placa) {
        String p = VehiculoServiceImpl.normalizarPlaca(placa);
        if (!vehiculoRepository.existsByPlacaIgnoreCase(p))
            throw new RecursoNoEncontradoException("Vehiculo", "placa", p);
        return registroRepository.historialPorPlaca(p).stream().map(DtoMapper::toResponse).toList();
    }

    /** RN-05: tolerancia de 10 min gratis; luego cada fraccion de hora se cobra como hora completa. */
    public static BigDecimal calcularMonto(long minutos, BigDecimal tarifaHora) {
        if (minutos <= MINUTOS_TOLERANCIA) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        long horas = (minutos + 59) / 60;
        return tarifaHora.multiply(BigDecimal.valueOf(horas)).setScale(2, RoundingMode.HALF_UP);
    }

    private Espacio asignarAutomaticamente(TipoVehiculo tipo) {
        Espacio libre = espacioRepository.findFirstByEstadoAndTipoOrderByCodigoAsc(EstadoEspacio.DISPONIBLE, tipo)
                .orElseThrow(() -> new EspacioNoDisponibleException("No hay espacios disponibles para vehiculos tipo " + tipo));
        return espacioRepository.buscarParaActualizar(libre.getId()).orElseThrow();
    }

    private Espacio validarEspacioSolicitado(Long espacioId, TipoVehiculo tipo) {
        Espacio e = espacioRepository.buscarParaActualizar(espacioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Espacio", "id", espacioId));
        if (e.getEstado() != EstadoEspacio.DISPONIBLE)
            throw new EspacioNoDisponibleException("El espacio " + e.getCodigo() + " no esta disponible (estado: " + e.getEstado() + ")");
        if (e.getTipo() != tipo)
            throw new ReglaNegocioException("TIPO_INCOMPATIBLE",
                    "El espacio " + e.getCodigo() + " es para " + e.getTipo() + " y el vehiculo es " + tipo);
        return e;
    }
}
