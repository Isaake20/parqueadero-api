package ec.edu.parqueadero.service;

import ec.edu.parqueadero.dto.*;
import java.util.List;

public interface UsuarioService {
    List<UsuarioResponse> listar();
    UsuarioResponse obtener(Long id);
    UsuarioResponse crear(UsuarioRequest request);
    UsuarioResponse actualizar(Long id, UsuarioRequest request);
    UsuarioResponse cambiarEstado(Long id, boolean activo);
    void eliminar(Long id);
}
