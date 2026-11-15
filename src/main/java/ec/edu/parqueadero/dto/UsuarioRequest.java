package ec.edu.parqueadero.dto;

import ec.edu.parqueadero.entity.TipoUsuario;
import jakarta.validation.constraints.*;

public record UsuarioRequest(
        @NotBlank(message = "La cedula es obligatoria")
        @Pattern(regexp = "\\d{10}", message = "La cedula debe tener exactamente 10 digitos")
        String cedula,
        @NotBlank(message = "Los nombres son obligatorios") @Size(max = 80) String nombres,
        @NotBlank(message = "Los apellidos son obligatorios") @Size(max = 80) String apellidos,
        @NotBlank(message = "El email es obligatorio") @Email(message = "El email no tiene un formato valido") String email,
        @Pattern(regexp = "^$|0\\d{8,9}", message = "El telefono debe iniciar con 0 y tener 9 o 10 digitos") String telefono,
        @NotNull(message = "El tipo de usuario es obligatorio (ESTUDIANTE, DOCENTE, ADMINISTRATIVO, VISITANTE)") TipoUsuario tipo
) {}
