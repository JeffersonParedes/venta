package pe.edu.promart.sistemaventas.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DetalleVentaDto {
    private Integer idProducto;
    private String nombreProducto;
    private Integer cantidad;
    private BigDecimal descuento;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private String productoCodigo;
}