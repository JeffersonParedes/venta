package pe.edu.promart.sistemaventas.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class VentaRequestDto {
    private Integer idCliente; // ID del cliente seleccionado
    private String tipoComprobante; // BOLETA o FACTURA
    private String metodoPago; // EFECTIVO, TARJETA, ETC
    private BigDecimal totalVenta;
    private BigDecimal descuentos;
    private BigDecimal montoRecibido;
    private BigDecimal cambio;
    private String observaciones;
    private List<DetalleVentaDto> detalles; // Lista de productos
}