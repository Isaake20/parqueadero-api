package ec.edu.parqueadero.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Registro de ingreso y salida de un vehiculo en un espacio. */
@Entity
@Table(name = "registros_estacionamiento",
        indexes = {
                @Index(name = "idx_registro_estado", columnList = "estado"),
                @Index(name = "idx_registro_ingreso", columnList = "fecha_ingreso")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RegistroEstacionamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehiculo_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_registro_vehiculo"))
    private Vehiculo vehiculo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "espacio_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_registro_espacio"))
    private Espacio espacio;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDateTime fechaIngreso;

    @Column(name = "fecha_salida")
    private LocalDateTime fechaSalida;

    @Column(name = "minutos_permanencia")
    private Long minutosPermanencia;

    @Column(name = "monto_cobrado", precision = 10, scale = 2)
    private BigDecimal montoCobrado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private EstadoRegistro estado;

    @Column(length = 200)
    private String observacion;
}
