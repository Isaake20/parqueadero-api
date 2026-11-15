package ec.edu.parqueadero.service;

import ec.edu.parqueadero.entity.AuditoriaOperacion;
import java.util.List;

public interface AuditoriaService {
    void registrar(AuditoriaOperacion auditoria);
    List<AuditoriaOperacion> ultimas(String resultado);
}
