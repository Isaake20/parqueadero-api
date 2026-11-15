package ec.edu.parqueadero.dto;

import ec.edu.parqueadero.entity.TipoVehiculo;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record EspacioRequest(
        @NotBlank(message = "El codigo es obligatorio")
        @Pattern(regexp = "(?i)^[A-Z]{1,2}-\\d{2,3}$", message = "Formato de codigo invalido. Ejemplo: A-01")
        String codigo,
        @NotBlank(message = "La zona es obligatoria") @Size(max = 30) String zona,
        @NotNull(message = "El tipo de espacio es obligatorio (AUTO, MOTO)") TipoVehiculo tipo,
        @NotNull(message = "La tarifa por hora es obligatoria")
        @DecimalMin(value = "0.01", message = "La tarifa por hora debe ser mayor que 0")
        @Digits(integer = 6, fraction = 2) BigDecimal tarifaHora
) {}
