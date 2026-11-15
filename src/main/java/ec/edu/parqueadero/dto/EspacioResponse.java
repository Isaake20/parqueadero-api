package ec.edu.parqueadero.dto;

import ec.edu.parqueadero.entity.EstadoEspacio;
import ec.edu.parqueadero.entity.TipoVehiculo;
import java.math.BigDecimal;

public record EspacioResponse(Long id, String codigo, String zona, TipoVehiculo tipo,
                              BigDecimal tarifaHora, EstadoEspacio estado) {}
