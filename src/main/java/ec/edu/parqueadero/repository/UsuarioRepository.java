package ec.edu.parqueadero.repository;

import ec.edu.parqueadero.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByCedula(String cedula);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByCedulaAndIdNot(String cedula, Long id);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    Optional<Usuario> findByCedula(String cedula);
}
