package pe.edu.promart.sistemaventas.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClienteBusquedaDto {
    private Integer idCliente;
    private String nombreCompleto; // nombres + apellidos o razon social
    private String documentoIdentidad;
    private String tipoDocumento;
}