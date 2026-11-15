package ec.edu.parqueadero.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Propietario o conductor registrado en el sistema de estacionamiento. */
@Entity
@Table(name = "usuarios",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_usuario_cedula", columnNames = "cedula"),
                @UniqueConstraint(name = "uk_usuario_email", columnNames = "email")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String cedula;

    @Column(nullable = false, length = 80)
    private String nombres;

    @Column(nullable = false, length = 80)
    private String apellidos;

    @Column(nullable = false, length = 120)
    private String email;

    @Column(length = 15)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoUsuario tipo;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @OneToMany(mappedBy = "usuario")
    @Builder.Default
    private List<Vehiculo> vehiculos = new ArrayList<>();

    @PrePersist
    void alCrear() {
        if (fechaRegistro == null) fechaRegistro = LocalDateTime.now();
        if (activo == null) activo = true;
    }
}
