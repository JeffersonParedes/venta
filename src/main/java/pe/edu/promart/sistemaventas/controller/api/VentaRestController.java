package pe.edu.promart.sistemaventas.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pe.edu.promart.sistemaventas.dto.VentaRequestDto;
import pe.edu.promart.sistemaventas.dto.VentaResponseDto;
import pe.edu.promart.sistemaventas.service.VentaService;

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
    public ResponseEntity<VentaResponseDto> registrarVenta(
            @RequestBody VentaRequestDto ventaRequest,
            @AuthenticationPrincipal UserDetails userDetails,
            @SessionAttribute("idCaja") Integer idCaja) {

        try {
            String username = userDetails.getUsername();

            // 2. Procesamiento transaccional
            // 🔑 CORRECCIÓN: Cambiamos el nombre del método a registrarVenta()
            VentaResponseDto response = ventaService.registrarVenta(ventaRequest, username, idCaja);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // Manejo de errores de negocio (Ej: Stock insuficiente, Cliente no encontrado)
            // Retorna un 400 Bad Request con el mensaje de error.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            // Error interno (Ej: Fallo en la BD)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Aquí puedes añadir otros métodos como buscar ventas o reportes.
}