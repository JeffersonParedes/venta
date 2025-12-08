package pe.edu.promart.sistemaventas.service; // ¡Paquete correcto!

import pe.edu.promart.sistemaventas.dto.VentaRequestDto;
import pe.edu.promart.sistemaventas.dto.VentaResponseDto;

// CAMBIAR 'class' POR 'interface'
public interface VentaService {

    // En una interfaz, los métodos son abstractos por defecto, no necesitan cuerpo.
    VentaResponseDto registrarVenta(VentaRequestDto dto, String username, Integer idCaja);

    // Nota: El cuerpo del método debe ser eliminado en la interfaz.
}
