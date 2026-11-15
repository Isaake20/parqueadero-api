package ec.edu.parqueadero.dto;

import ec.edu.parqueadero.entity.EstadoRegistro;
import ec.edu.parqueadero.entity.TipoVehiculo;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RegistroResponse(Long id, String placa, TipoVehiculo tipoVehiculo, String codigoEspacio,
                               LocalDateTime fechaIngreso, LocalDateTime fechaSalida, Long minutosPermanencia,
                               String tiempoPermanencia, BigDecimal montoCobrado, EstadoRegistro estado,
                               String observacion) {}
