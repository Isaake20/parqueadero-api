package ec.edu.parqueadero.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Traza generada automaticamente por el aspecto de auditoria. */
@Entity
@Table(name = "auditoria_operaciones")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditoriaOperacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String operacion;

    @Column(nullable = false, length = 150)
    private String metodo;

    @Column(length = 500)
    private String argumentos;

    @Column(nullable = false, length = 10)
    private String resultado;

    @Column(name = "mensaje_error", length = 300)
    private String mensajeError;

    @Column(name = "duracion_ms", nullable = false)
    private Long duracionMs;

    @Column(name = "request_id", length = 40)
    private String requestId;

    @Column(nullable = false)
    private LocalDateTime fecha;
}
