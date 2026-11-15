package ec.edu.parqueadero.service.impl;

import ec.edu.parqueadero.entity.AuditoriaOperacion;
import ec.edu.parqueadero.repository.AuditoriaOperacionRepository;
import ec.edu.parqueadero.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditoriaServiceImpl implements AuditoriaService {

    private final AuditoriaOperacionRepository repository;

    /** REQUIRES_NEW: la auditoria se guarda aunque la transaccion de negocio haga rollback. */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(AuditoriaOperacion auditoria) {
        repository.save(auditoria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditoriaOperacion> ultimas(String resultado) {
        return resultado == null ? repository.findTop50ByOrderByIdDesc()
                : repository.findByResultadoOrderByIdDesc(resultado.toUpperCase());
    }
}
