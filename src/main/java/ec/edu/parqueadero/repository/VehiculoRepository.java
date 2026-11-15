package ec.edu.parqueadero.repository;

import ec.edu.parqueadero.entity.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    /** Consulta derivada: busca por placa sin distinguir mayusculas. */
    Optional<Vehiculo> findByPlacaIgnoreCase(String placa);

    boolean existsByPlacaIgnoreCase(String placa);

    boolean existsByPlacaIgnoreCaseAndIdNot(String placa, Long id);

    /** Consulta JPQL personalizada: vehiculos de un usuario con fetch del propietario. */
    @Query("SELECT v FROM Vehiculo v JOIN FETCH v.usuario u WHERE u.id = :usuarioId ORDER BY v.placa")
    List<Vehiculo> buscarPorUsuario(@Param("usuarioId") Long usuarioId);

    long countByUsuarioId(Long usuarioId);
}
