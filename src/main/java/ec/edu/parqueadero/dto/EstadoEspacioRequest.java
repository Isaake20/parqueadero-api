package ec.edu.parqueadero.dto;

import ec.edu.parqueadero.entity.EstadoEspacio;
import jakarta.validation.constraints.NotNull;

public record EstadoEspacioRequest(
        @NotNull(message = "El estado es obligatorio (DISPONIBLE, MANTENIMIENTO)") EstadoEspacio estado) {}
