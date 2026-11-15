package ec.edu.parqueadero.repository;

import ec.edu.parqueadero.entity.EstadoEspacio;
import ec.edu.parqueadero.entity.Espacio;
import ec.edu.parqueadero.entity.TipoVehiculo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EspacioRepository extends JpaRepository<Espacio, Long> {

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

    /** Consulta derivada: espacios por estado y tipo ordenados por codigo. */
    List<Espacio> findByEstadoAndTipoOrderByCodigoAsc(EstadoEspacio estado, TipoVehiculo tipo);

    List<Espacio> findByEstadoOrderByCodigoAsc(EstadoEspacio estado);

    /** Primer espacio libre compatible (asignacion automatica). */
    Optional<Espacio> findFirstByEstadoAndTipoOrderByCodigoAsc(EstadoEspacio estado, TipoVehiculo tipo);

    /** Bloqueo pesimista para evitar que dos ingresos simultaneos tomen el mismo espacio. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Espacio e WHERE e.id = :id")
    Optional<Espacio> buscarParaActualizar(@Param("id") Long id);

    /** Consulta JPQL agregada: resumen de ocupacion por tipo y estado. */
    @Query("SELECT e.tipo AS tipo, e.estado AS estado, COUNT(e) AS total " +
           "FROM Espacio e GROUP BY e.tipo, e.estado ORDER BY e.tipo, e.estado")
    List<ResumenOcupacion> resumenOcupacion();

    /** Proyeccion de interfaz para el resumen de ocupacion. */
    interface ResumenOcupacion {
        TipoVehiculo getTipo();
        EstadoEspacio getEstado();
        Long getTotal();
    }
}
