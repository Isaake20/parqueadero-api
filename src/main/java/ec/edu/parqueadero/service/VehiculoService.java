package ec.edu.parqueadero.service;

import ec.edu.parqueadero.dto.*;
import java.util.List;

public interface VehiculoService {
    List<VehiculoResponse> listar();
    VehiculoResponse obtener(Long id);
    VehiculoResponse obtenerPorPlaca(String placa);
    List<VehiculoResponse> listarPorUsuario(Long usuarioId);
    VehiculoResponse crear(VehiculoRequest request);
    VehiculoResponse actualizar(Long id, VehiculoRequest request);
    void eliminar(Long id);
}
