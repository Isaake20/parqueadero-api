package ec.edu.parqueadero.repository;

import ec.edu.parqueadero.entity.EstadoRegistro;
import ec.edu.parqueadero.entity.RegistroEstacionamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RegistroEstacionamientoRepository extends JpaRepository<RegistroEstacionamiento, Long> {

    boolean existsByVehiculoIdAndEstado(Long vehiculoId, EstadoRegistro estado);

    boolean existsByEspacioIdAndEstado(Long espacioId, EstadoRegistro estado);

    boolean existsByVehiculoId(Long vehiculoId);

    boolean existsByEspacioId(Long espacioId);

    /** JPQL con JOIN FETCH: registros activos (vehiculos dentro del parqueadero). */
    @Query("SELECT r FROM RegistroEstacionamiento r JOIN FETCH r.vehiculo v JOIN FETCH r.espacio e " +
           "WHERE r.estado = ec.edu.parqueadero.entity.EstadoRegistro.ACTIVO ORDER BY r.fechaIngreso")
    List<RegistroEstacionamiento> buscarActivos();

    @Query("SELECT r FROM RegistroEstacionamiento r JOIN FETCH r.vehiculo v JOIN FETCH r.espacio e WHERE r.id = :id")
    Optional<RegistroEstacionamiento> buscarDetalle(@Param("id") Long id);

    /** JPQL: historial de un vehiculo por placa. */
    @Query("SELECT r FROM RegistroEstacionamiento r JOIN FETCH r.vehiculo v JOIN FETCH r.espacio e " +
           "WHERE UPPER(v.placa) = UPPER(:placa) ORDER BY r.fechaIngreso DESC")
    List<RegistroEstacionamiento> historialPorPlaca(@Param("placa") String placa);

    /** JPQL: registros finalizados dentro de un rango de fechas. */
    @Query("SELECT r FROM RegistroEstacionamiento r JOIN FETCH r.vehiculo v JOIN FETCH r.espacio e " +
           "WHERE r.estado = :estado AND r.fechaSalida BETWEEN :desde AND :hasta ORDER BY r.fechaSalida")
    List<RegistroEstacionamiento> finalizadosEntre(@Param("estado") EstadoRegistro estado,
                                                   @Param("desde") LocalDateTime desde,
                                                   @Param("hasta") LocalDateTime hasta);

    /** JPQL agregada: total recaudado en un rango. */
    @Query("SELECT COALESCE(SUM(r.montoCobrado), 0) FROM RegistroEstacionamiento r " +
           "WHERE r.estado = ec.edu.parqueadero.entity.EstadoRegistro.FINALIZADO " +
           "AND r.fechaSalida BETWEEN :desde AND :hasta")
    BigDecimal totalRecaudado(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
