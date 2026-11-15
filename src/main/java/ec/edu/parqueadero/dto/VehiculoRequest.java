package ec.edu.parqueadero.dto;

import ec.edu.parqueadero.entity.TipoVehiculo;
import jakarta.validation.constraints.*;

public record VehiculoRequest(
        @NotBlank(message = "La placa es obligatoria")
        @Pattern(regexp = "(?i)^[A-Z]{3}-?\\d{3,4}$|^(?i)[A-Z]{2}\\d{3}[A-Z]$",
                 message = "Formato de placa invalido. Ejemplos: PBA-1234 (auto) o IB123C (moto)")
        String placa,
        @NotBlank(message = "La marca es obligatoria") @Size(max = 40) String marca,
        @NotBlank(message = "El modelo es obligatorio") @Size(max = 40) String modelo,
        @Size(max = 30) String color,
        @NotNull(message = "El tipo de vehiculo es obligatorio (AUTO, MOTO)") TipoVehiculo tipo,
        @NotNull(message = "El id del usuario propietario es obligatorio") @Positive Long usuarioId
) {}
