package ec.edu.parqueadero.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Si espacioId es null el sistema asigna automaticamente el primer espacio libre compatible. */
public record IngresoRequest(
        @NotBlank(message = "La placa del vehiculo es obligatoria") String placa,
        @Positive(message = "El id del espacio debe ser positivo") Long espacioId,
        @Size(max = 200) String observacion
) {}
