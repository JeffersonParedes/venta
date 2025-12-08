package pe.edu.promart.sistemaventas.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class ProductoBusquedaDto {
    private Long id;
    private String codigo;
    private String nombre;
    private BigDecimal precioVenta; // Precio final (asumimos con IGV incluido)
    private Integer stock;
}
