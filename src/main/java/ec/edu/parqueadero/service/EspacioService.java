package ec.edu.parqueadero.service;

import ec.edu.parqueadero.dto.*;
import ec.edu.parqueadero.entity.EstadoEspacio;
import ec.edu.parqueadero.entity.TipoVehiculo;
import java.util.List;

public interface EspacioService {
    List<EspacioResponse> listar(EstadoEspacio estado);
    EspacioResponse obtener(Long id);
    List<EspacioResponse> listarDisponibles(TipoVehiculo tipo);
    DisponibilidadResponse disponibilidad();
    EspacioResponse crear(EspacioRequest request);
    EspacioResponse actualizar(Long id, EspacioRequest request);
    EspacioResponse cambiarEstado(Long id, EstadoEspacio estado);
    void eliminar(Long id);
}
