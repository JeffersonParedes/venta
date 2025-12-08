package pe.edu.promart.sistemaventas.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "detalleventa")
public class DetalleVenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Integer idDetalle;

    @ManyToOne
    @JoinColumn(name = "id_venta")
    private Venta venta;

    @ManyToOne
    @JoinColumn(name = "id_producto")
    private Producto producto;

    private Integer cantidad;

    @Column(name = "precio_unitario")
    private BigDecimal precioUnitario;

    // 🔑 CAMPO AÑADIDO PARA RESOLVER setDescuentoLinea()
    @Column(name = "descuento_linea")
    private BigDecimal descuentoLinea;

    private BigDecimal subtotal;
}