package pe.edu.promart.sistemaventas.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.promart.sistemaventas.dto.VentaResponseDto;
import pe.edu.promart.sistemaventas.entity.Venta;
import pe.edu.promart.sistemaventas.repository.VentaRepository;
import pe.edu.promart.sistemaventas.service.PdfReportService;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/historial")
public class HistorialRestController {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private PdfReportService pdfReportService;

    @GetMapping("/listar")
    public ResponseEntity<List<VentaResponseDto>> listarVentas(
            @SessionAttribute(name = "SESSION_CAJA_ID", required = false) Integer idCaja,
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {

        if (idCaja == null) {
            return ResponseEntity.status(401).build(); // No autorizado si no hay caja en sesión
        }

        List<Venta> ventas = ventaRepository.buscarHistorialPorCaja(idCaja, fechaInicio, fechaFin);

        // Mapeo manual rápido a DTO para responder JSON
        List<VentaResponseDto> dtos = ventas.stream().map(v -> VentaResponseDto.builder()
                .idVenta(v.getIdVenta())
                .nroVenta(v.getNumeroVenta())
                .tipoComprobante(v.getTipoComprobante())
                .fecha(v.getFecha())
                .clienteNombre(v.getCliente().getNombres() + " " + v.getCliente().getApellidos())
                .metodoPago(v.getMetodoPago())
                .totalPagado(v.getTotal())
                .detalleResumen(new ArrayList<>()) // No necesitamos detalles para la tabla simple
                .build() // Usando el builder existente, campo 'estado' se podría añadir si el DTO lo
                         // soporta, si no, se asume.
        ).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/exportar-pdf")
    public ResponseEntity<byte[]> exportarPdf(
            @SessionAttribute(name = "SESSION_CAJA_ID", required = false) Integer idCaja,
            @SessionAttribute(name = "nombreUsuario", required = false) String nombreUsuario, // Asumimos que guardamos
                                                                                              // el nombre en sesión
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            HttpSession session) throws IOException {

        if (idCaja == null) {
            return ResponseEntity.status(401).build();
        }

        List<Venta> ventas = ventaRepository.buscarHistorialPorCaja(idCaja, fechaInicio, fechaFin);

        // Datos de contexto para el reporte (simulados o desde sesión)
        String nombreSucursal = "SUCURSAL PRINCIPAL"; // Idealmente obtener de sesión o BD
        String numeroCaja = "CAJA-" + idCaja;
        String usuario = (nombreUsuario != null) ? nombreUsuario : "Usuario";

        byte[] pdfBytes = pdfReportService.generarReporteVentas(ventas, nombreSucursal, numeroCaja, usuario);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=historial_ventas.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
