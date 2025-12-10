package pe.edu.promart.sistemaventas.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pe.edu.promart.sistemaventas.dto.VentaRequestDto;
import pe.edu.promart.sistemaventas.dto.VentaResponseDto;
import pe.edu.promart.sistemaventas.service.VentaService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ventas")
public class VentaRestController {

    @Autowired
    private VentaService ventaService;

    /**
     * Endpoint para procesar y registrar una nueva venta.
     * Recibe el VentaRequestDto desde el frontend (POS).
     * 
     * @param ventaRequest DTO con los datos de la venta y los detalles.
     * @param userDetails  Objeto de seguridad para obtener el username del
     *                     vendedor.
     * @param idCaja       ID de la caja seleccionada, obtenido de la sesión.
     * @return ResponseEntity con el resumen de la venta (VentaResponseDto).
     */
    @PostMapping("/procesar")
    public ResponseEntity<?> registrarVenta(
            @RequestBody VentaRequestDto ventaRequest,
            @AuthenticationPrincipal UserDetails userDetails,
            @SessionAttribute(name = "SESSION_CAJA_ID", required = false) Integer idCaja) {

        try {
            // Validación inicial
            if (ventaRequest == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "El request no puede ser nulo");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorResponse);
            }
            
            // Validar que la caja esté en la sesión
            if (idCaja == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "No hay una caja seleccionada en la sesión. Por favor, selecciona una caja primero.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorResponse);
            }
            
            System.out.println("Recibida solicitud de venta: " + ventaRequest);
            System.out.println("ID de caja de la sesión: " + idCaja);
            
            String username = userDetails.getUsername();

            // 2. Procesamiento transaccional
            // 🔑 CORRECCIÓN: Cambiamos el nombre del método a registrarVenta()
            VentaResponseDto response = ventaService.registrarVenta(ventaRequest, username, idCaja);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // Manejo de errores de negocio (Ej: Stock insuficiente, Cliente no encontrado)
            // Retorna un 400 Bad Request con el mensaje de error.
            String errorMsg = e.getMessage() != null ? e.getMessage() : "Error desconocido";
            
            // Simplificar mensajes de error de base de datos
            if (errorMsg.contains("constraint") || errorMsg.contains("violates check constraint")) {
                errorMsg = "Error de configuración en la base de datos. Contacte al administrador del sistema.";
            }
            
            System.err.println("========================================");
            System.err.println("ERROR AL PROCESAR VENTA (RuntimeException)");
            System.err.println("Mensaje: " + errorMsg);
            System.err.println("Clase: " + e.getClass().getName());
            System.err.println("========================================");
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", errorMsg);
            System.err.println("Respuesta de error que se enviará: " + errorResponse);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        } catch (Exception e) {
            // Error interno (Ej: Fallo en la BD)
            String errorMsg = e.getMessage() != null ? e.getMessage() : "Error desconocido";
            
            // Simplificar mensajes de error de base de datos
            if (errorMsg.contains("constraint") || errorMsg.contains("violates check constraint") || 
                errorMsg.contains("could not execute statement")) {
                errorMsg = "Error de configuración en la base de datos. Contacte al administrador del sistema.";
            }
            
            System.err.println("Error interno al procesar venta: " + errorMsg);
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", errorMsg);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
    }

    // Aquí puedes añadir otros métodos como buscar ventas o reportes.
}