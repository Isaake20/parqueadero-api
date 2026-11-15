package ec.edu.parqueadero.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;

/** Plaza fisica del estacionamiento. */
@Entity
@Table(name = "espacios",
        uniqueConstraints = @UniqueConstraint(name = "uk_espacio_codigo", columnNames = "codigo"))
@Check(constraints = "tarifa_hora > 0")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Espacio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String codigo;

    @Column(nullable = false, length = 30)
    private String zona;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoVehiculo tipo;

    @Column(name = "tarifa_hora", nullable = false, precision = 8, scale = 2)
    private BigDecimal tarifaHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private EstadoEspacio estado = EstadoEspacio.DISPONIBLE;
}
