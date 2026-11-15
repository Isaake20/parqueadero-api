package ec.edu.parqueadero.dto;

import ec.edu.parqueadero.entity.TipoVehiculo;

public record VehiculoResponse(Long id, String placa, String marca, String modelo, String color,
                               TipoVehiculo tipo, Long usuarioId, String propietario) {}
