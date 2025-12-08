package pe.edu.promart.sistemaventas.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class VentaResponseDto {
    private Integer idVenta;
    private String nroVenta;

    // Campos existentes
    private Integer idCliente;
    private String tipoComprobante;
    private LocalDateTime fecha;
    private String metodoPago;
    private String clienteNombre;

    // 🔑 CAMPOS AÑADIDOS/CORREGIDOS PARA EL RESUMEN FINANCIERO:
    // Sustituimos 'totalVenta' (que no se usa) por los campos específicos:

    private BigDecimal totalPagado; // Campo necesario para .totalPagado()
    private BigDecimal montoRecibido; // Campo necesario para .montoRecibido()
    private BigDecimal cambio; // Campo necesario para .cambio()

    // 🔑 CORRECCIÓN: El campo en el DTO debe llamarse igual que el método del
    // builder
    // Cambiar 'detalles' por 'detalleResumen'
    private List<DetalleVentaDto> detalleResumen;

    // Si necesitas los otros campos (serie, totalVenta, estado, etc.), inclúyelos:
    private String serieComprobante;
    private String numeroComprobante;
    private BigDecimal totalVenta;
    private String estado;
    private String observaciones;
    private String empleadoNombre;
    private String cajaNombre;
    private String sucursalNombre;
}