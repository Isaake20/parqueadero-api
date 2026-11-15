package ec.edu.parqueadero.service;

import ec.edu.parqueadero.dto.IngresoRequest;
import ec.edu.parqueadero.dto.RegistroResponse;
import ec.edu.parqueadero.entity.*;
import ec.edu.parqueadero.exception.EspacioNoDisponibleException;
import ec.edu.parqueadero.exception.ReglaNegocioException;
import ec.edu.parqueadero.repository.*;
import ec.edu.parqueadero.service.impl.RegistroServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Pruebas unitarias de las reglas de negocio de ingreso y salida (sin base de datos). */
@ExtendWith(MockitoExtension.class)
class RegistroServiceImplTest {

    @Mock RegistroEstacionamientoRepository registroRepository;
    @Mock VehiculoRepository vehiculoRepository;
    @Mock EspacioRepository espacioRepository;
    @Mock NotificacionService notificacionService;

    private final ZoneId zona = ZoneId.of("America/Guayaquil");
    private final Clock reloj = Clock.fixed(LocalDateTime.of(2026, 11, 10, 12, 0).atZone(zona).toInstant(), zona);
    private RegistroServiceImpl service;
    private Vehiculo auto;
    private Espacio espacioAuto;

    @BeforeEach
    void setUp() {
        service = new RegistroServiceImpl(registroRepository, vehiculoRepository, espacioRepository, notificacionService, reloj);
        Usuario usuario = Usuario.builder().id(1L).nombres("Isaac").apellidos("Betun").activo(true).build();
        auto = Vehiculo.builder().id(1L).placa("PBA-1234").tipo(TipoVehiculo.AUTO).usuario(usuario).build();
        espacioAuto = Espacio.builder().id(1L).codigo("A-01").tipo(TipoVehiculo.AUTO)
                .tarifaHora(new BigDecimal("1.00")).estado(EstadoEspacio.DISPONIBLE).build();
    }

    @Test
    @DisplayName("RN-05: dentro de la tolerancia de 10 minutos no se cobra")
    void montoDentroDeTolerancia() {
        assertThat(RegistroServiceImpl.calcularMonto(10, new BigDecimal("1.00"))).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("RN-05: cada fraccion de hora se cobra como hora completa")
    void montoPorFraccionDeHora() {
        assertThat(RegistroServiceImpl.calcularMonto(11, new BigDecimal("1.00"))).isEqualByComparingTo("1.00");
        assertThat(RegistroServiceImpl.calcularMonto(60, new BigDecimal("1.00"))).isEqualByComparingTo("1.00");
        assertThat(RegistroServiceImpl.calcularMonto(61, new BigDecimal("1.00"))).isEqualByComparingTo("2.00");
        assertThat(RegistroServiceImpl.calcularMonto(135, new BigDecimal("0.50"))).isEqualByComparingTo("1.50");
    }

    @Test
    @DisplayName("Ingreso exitoso: ocupa el espacio y crea un registro ACTIVO")
    void ingresoExitoso() {
        when(vehiculoRepository.findByPlacaIgnoreCase("PBA-1234")).thenReturn(Optional.of(auto));
        when(registroRepository.existsByVehiculoIdAndEstado(1L, EstadoRegistro.ACTIVO)).thenReturn(false);
        when(espacioRepository.buscarParaActualizar(1L)).thenReturn(Optional.of(espacioAuto));
        when(registroRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegistroResponse r = service.registrarIngreso(new IngresoRequest("pba1234", 1L, null));

        assertThat(r.estado()).isEqualTo(EstadoRegistro.ACTIVO);
        assertThat(r.fechaIngreso()).isEqualTo(LocalDateTime.of(2026, 11, 10, 12, 0));
        assertThat(espacioAuto.getEstado()).isEqualTo(EstadoEspacio.OCUPADO);
    }

    @Test
    @DisplayName("RN-02: un vehiculo dentro del parqueadero no puede ingresar otra vez")
    void ingresoDuplicado() {
        when(vehiculoRepository.findByPlacaIgnoreCase("PBA-1234")).thenReturn(Optional.of(auto));
        when(registroRepository.existsByVehiculoIdAndEstado(1L, EstadoRegistro.ACTIVO)).thenReturn(true);

        assertThatThrownBy(() -> service.registrarIngreso(new IngresoRequest("PBA-1234", 1L, null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ya se encuentra dentro");
        verify(registroRepository, never()).save(any());
    }

    @Test
    @DisplayName("RN-03: no se puede ingresar a un espacio ocupado")
    void espacioOcupado() {
        espacioAuto.setEstado(EstadoEspacio.OCUPADO);
        when(vehiculoRepository.findByPlacaIgnoreCase("PBA-1234")).thenReturn(Optional.of(auto));
        when(espacioRepository.buscarParaActualizar(1L)).thenReturn(Optional.of(espacioAuto));

        assertThatThrownBy(() -> service.registrarIngreso(new IngresoRequest("PBA-1234", 1L, null)))
                .isInstanceOf(EspacioNoDisponibleException.class);
    }

    @Test
    @DisplayName("RN-03: el tipo de espacio debe coincidir con el tipo de vehiculo")
    void tipoIncompatible() {
        espacioAuto.setTipo(TipoVehiculo.MOTO);
        when(vehiculoRepository.findByPlacaIgnoreCase("PBA-1234")).thenReturn(Optional.of(auto));
        when(espacioRepository.buscarParaActualizar(1L)).thenReturn(Optional.of(espacioAuto));

        assertThatThrownBy(() -> service.registrarIngreso(new IngresoRequest("PBA-1234", 1L, null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("es para MOTO");
    }

    @Test
    @DisplayName("RN-04: un usuario inactivo no puede registrar ingresos")
    void usuarioInactivo() {
        auto.getUsuario().setActivo(false);
        when(vehiculoRepository.findByPlacaIgnoreCase("PBA-1234")).thenReturn(Optional.of(auto));

        assertThatThrownBy(() -> service.registrarIngreso(new IngresoRequest("PBA-1234", null, null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("inactivo");
    }

    @Test
    @DisplayName("Salida: calcula 2 h 15 min, cobra 3 horas y libera el espacio")
    void salidaCalculaMonto() {
        espacioAuto.setEstado(EstadoEspacio.OCUPADO);
        RegistroEstacionamiento activo = RegistroEstacionamiento.builder().id(5L).vehiculo(auto).espacio(espacioAuto)
                .fechaIngreso(LocalDateTime.of(2026, 11, 10, 9, 45)).estado(EstadoRegistro.ACTIVO).build();
        when(registroRepository.buscarDetalle(5L)).thenReturn(Optional.of(activo));
        when(registroRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegistroResponse r = service.registrarSalida(5L, true);

        assertThat(r.minutosPermanencia()).isEqualTo(135);
        assertThat(r.tiempoPermanencia()).isEqualTo("2 h 15 min");
        assertThat(r.montoCobrado()).isEqualByComparingTo("3.00");
        assertThat(espacioAuto.getEstado()).isEqualTo(EstadoEspacio.DISPONIBLE);
        verify(notificacionService).enviarComprobanteAsync(any());
    }

    @Test
    @DisplayName("Salida: no se puede registrar dos veces la salida")
    void salidaDuplicada() {
        RegistroEstacionamiento fin = RegistroEstacionamiento.builder().id(6L).vehiculo(auto).espacio(espacioAuto)
                .fechaIngreso(LocalDateTime.of(2026, 11, 10, 8, 0)).fechaSalida(LocalDateTime.of(2026, 11, 10, 9, 0))
                .estado(EstadoRegistro.FINALIZADO).build();
        when(registroRepository.buscarDetalle(6L)).thenReturn(Optional.of(fin));

        assertThatThrownBy(() -> service.registrarSalida(6L, true)).isInstanceOf(ReglaNegocioException.class);
    }
}
