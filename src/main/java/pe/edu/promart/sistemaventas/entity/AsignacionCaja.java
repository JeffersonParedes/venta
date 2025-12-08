package pe.edu.promart.sistemaventas.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "AsignacionCaja", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id_empleado", "id_caja", "activa"})
})
public class AsignacionCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asignacion")
    private Integer idAsignacion;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado", nullable = false)
    private Empleado empleado;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_caja", nullable = false)
    private Caja caja;

    @Column(name = "fecha_asignacion")
    private LocalDateTime fechaAsignacion;

    @Column(name = "fecha_desasignacion")
    private LocalDateTime fechaDesasignacion;

    @NotNull
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String turno;

    @Column(name = "activa", columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean activa = true;

    @PrePersist
    protected void onCreate() {
        if (this.fechaAsignacion == null) {
            this.fechaAsignacion = LocalDateTime.now();
        }
    }
}
