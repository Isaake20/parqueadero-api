package ec.edu.parqueadero.service;

import ec.edu.parqueadero.dto.*;
import java.util.List;

public interface RegistroService {
    RegistroResponse registrarIngreso(IngresoRequest request);
    RegistroResponse registrarSalida(Long registroId, boolean notificacionAsincrona);
    RegistroResponse obtener(Long id);
    List<RegistroResponse> listarActivos();
    List<RegistroResponse> historialPorPlaca(String placa);
}
