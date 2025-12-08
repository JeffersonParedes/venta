package pe.edu.promart.sistemaventas.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "producto")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    private String codigo;
    private String nombre;
    private BigDecimal precio;
    
    @Column(name = "stock_actual")
    private Integer stockActual;
}