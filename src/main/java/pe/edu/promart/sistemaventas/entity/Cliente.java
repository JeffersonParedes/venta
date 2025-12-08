package pe.edu.promart.sistemaventas.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Integer idCliente;

    @NotNull
    @Size(max = 20)
    @Column(name = "tipo_cliente", nullable = false, length = 20)
    private String tipoCliente;

    @Size(max = 100)
    @Column(length = 100)
    private String nombres;

    @Size(max = 100)
    @Column(length = 100)
    private String apellidos;

    @Size(max = 150)
    @Column(name = "razon_social", length = 150)
    private String razonSocial;

    @NotNull
    @Size(max = 10)
    @Column(name = "tipo_documento", nullable = false, length = 10)
    private String tipoDocumento;

    @NotNull
    @Size(max = 20)
    @Column(name = "documento_identidad", nullable = false, unique = true, length = 20)
    private String documentoIdentidad;

    @Email
    @Size(max = 100)
    @Column(unique = true, length = 100)
    private String email;

    @Size(max = 20)
    @Column(length = 20)
    private String telefono;

    @Column(name = "direccion_principal", columnDefinition = "TEXT")
    private String direccionPrincipal;

    @Size(max = 50)
    @Column(length = 50)
    private String segmento;

    @Size(max = 50)
    @Column(name = "canal_origen", length = 50)
    private String canalOrigen;

    @Column(name = "es_publico_general", columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean esPublicoGeneral = false;

    @Column(name = "fecha_primera_compra")
    private LocalDate fechaPrimeraCompra;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "estado", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean estado = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.fechaRegistro == null) {
            this.fechaRegistro = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
