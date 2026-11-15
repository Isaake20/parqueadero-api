package ec.edu.parqueadero.repository;

import ec.edu.parqueadero.entity.AuditoriaOperacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditoriaOperacionRepository extends JpaRepository<AuditoriaOperacion, Long> {

    List<AuditoriaOperacion> findTop50ByOrderByIdDesc();

    List<AuditoriaOperacion> findByResultadoOrderByIdDesc(String resultado);
}
