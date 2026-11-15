package ec.edu.parqueadero.entity;

import jakarta.persistence.*;
import lombok.*;

/** Vehiculo perteneciente a un usuario. La placa es unica en el sistema. */
@Entity
@Table(name = "vehiculos",
        uniqueConstraints = @UniqueConstraint(name = "uk_vehiculo_placa", columnNames = "placa"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 8)
    private String placa;

    @Column(nullable = false, length = 40)
    private String marca;

    @Column(nullable = false, length = 40)
    private String modelo;

    @Column(length = 30)
    private String color;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoVehiculo tipo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_vehiculo_usuario"))
    private Usuario usuario;
}
