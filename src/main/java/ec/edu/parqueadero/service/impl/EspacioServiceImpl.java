package ec.edu.parqueadero.service.impl;

import ec.edu.parqueadero.aspect.Auditable;
import ec.edu.parqueadero.dto.*;
import ec.edu.parqueadero.entity.*;
import ec.edu.parqueadero.exception.*;
import ec.edu.parqueadero.repository.EspacioRepository;
import ec.edu.parqueadero.repository.RegistroEstacionamientoRepository;
import ec.edu.parqueadero.service.EspacioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EspacioServiceImpl implements EspacioService {

    private final EspacioRepository espacioRepository;
    private final RegistroEstacionamientoRepository registroRepository;

    @Override
    public List<EspacioResponse> listar(EstadoEspacio estado) {
        List<Espacio> espacios = estado == null ? espacioRepository.findAll()
                : espacioRepository.findByEstadoOrderByCodigoAsc(estado);
        return espacios.stream().sorted(Comparator.comparing(Espacio::getCodigo)).map(DtoMapper::toResponse).toList();
    }

    @Override
    public EspacioResponse obtener(Long id) {
        return DtoMapper.toResponse(buscar(id));
    }

    @Override
    public List<EspacioResponse> listarDisponibles(TipoVehiculo tipo) {
        List<Espacio> lista = tipo == null
                ? espacioRepository.findByEstadoOrderByCodigoAsc(EstadoEspacio.DISPONIBLE)
                : espacioRepository.findByEstadoAndTipoOrderByCodigoAsc(EstadoEspacio.DISPONIBLE, tipo);
        return lista.stream().map(DtoMapper::toResponse).toList();
    }

    @Override
    public DisponibilidadResponse disponibilidad() {
        Map<String, Map<String, Long>> detalle = new LinkedHashMap<>();
        long total = 0, disp = 0, ocup = 0, mant = 0;
        for (EspacioRepository.ResumenOcupacion r : espacioRepository.resumenOcupacion()) {
            detalle.computeIfAbsent(r.getTipo().name(), k -> new LinkedHashMap<>()).put(r.getEstado().name(), r.getTotal());
            total += r.getTotal();
            switch (r.getEstado()) {
                case DISPONIBLE -> disp += r.getTotal();
                case OCUPADO -> ocup += r.getTotal();
                case MANTENIMIENTO -> mant += r.getTotal();
            }
        }
        double porcentaje = total == 0 ? 0 : Math.round(ocup * 10000.0 / total) / 100.0;
        return new DisponibilidadResponse(total, disp, ocup, mant, porcentaje, detalle);
    }

    @Override
    @Transactional
    @Auditable(operacion = "CREAR_ESPACIO")
    public EspacioResponse crear(EspacioRequest r) {
        String codigo = r.codigo().trim().toUpperCase();
        if (espacioRepository.existsByCodigoIgnoreCase(codigo))
            throw new RecursoDuplicadoException("espacio", "codigo", codigo);
        Espacio e = Espacio.builder().codigo(codigo).zona(r.zona().trim()).tipo(r.tipo())
                .tarifaHora(r.tarifaHora()).estado(EstadoEspacio.DISPONIBLE).build();
        return DtoMapper.toResponse(espacioRepository.save(e));
    }

    @Override
    @Transactional
    @Auditable(operacion = "ACTUALIZAR_ESPACIO")
    public EspacioResponse actualizar(Long id, EspacioRequest r) {
        Espacio e = buscar(id);
        String codigo = r.codigo().trim().toUpperCase();
        if (espacioRepository.existsByCodigoIgnoreCaseAndIdNot(codigo, id))
            throw new RecursoDuplicadoException("espacio", "codigo", codigo);
        if (e.getEstado() == EstadoEspacio.OCUPADO && e.getTipo() != r.tipo())
            throw new ReglaNegocioException("ESPACIO_OCUPADO", "No se puede cambiar el tipo de un espacio ocupado");
        e.setCodigo(codigo);
        e.setZona(r.zona().trim());
        e.setTipo(r.tipo());
        e.setTarifaHora(r.tarifaHora());
        return DtoMapper.toResponse(espacioRepository.save(e));
    }

    @Override
    @Transactional
    @Auditable(operacion = "CAMBIAR_ESTADO_ESPACIO")
    public EspacioResponse cambiarEstado(Long id, EstadoEspacio nuevo) {
        Espacio e = buscar(id);
        if (nuevo == EstadoEspacio.OCUPADO)
            throw new ReglaNegocioException("ESTADO_NO_PERMITIDO",
                    "El estado OCUPADO solo se asigna automaticamente al registrar un ingreso");
        if (e.getEstado() == EstadoEspacio.OCUPADO)
            throw new ReglaNegocioException("ESPACIO_OCUPADO",
                    "El espacio " + e.getCodigo() + " esta ocupado; registre primero la salida del vehiculo");
        e.setEstado(nuevo);
        return DtoMapper.toResponse(espacioRepository.save(e));
    }

    @Override
    @Transactional
    @Auditable(operacion = "ELIMINAR_ESPACIO")
    public void eliminar(Long id) {
        Espacio e = buscar(id);
        if (e.getEstado() == EstadoEspacio.OCUPADO)
            throw new ReglaNegocioException("ESPACIO_OCUPADO", "No se puede eliminar el espacio " + e.getCodigo() + " porque esta ocupado");
        if (registroRepository.existsByEspacioId(id))
            throw new ReglaNegocioException("ESPACIO_CON_HISTORIAL",
                    "No se puede eliminar el espacio " + e.getCodigo() + " porque tiene historial; cambie su estado a MANTENIMIENTO");
        espacioRepository.delete(e);
    }

    private Espacio buscar(Long id) {
        return espacioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Espacio", "id", id));
    }
}
