package ec.edu.parqueadero.dto;

import ec.edu.parqueadero.entity.TipoUsuario;
import java.time.LocalDateTime;

public record UsuarioResponse(Long id, String cedula, String nombres, String apellidos, String email,
                              String telefono, TipoUsuario tipo, Boolean activo, LocalDateTime fechaRegistro) {}
