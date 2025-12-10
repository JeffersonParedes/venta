package pe.edu.promart.sistemaventas.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import pe.edu.promart.sistemaventas.entity.Venta;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfReportService {

    public byte[] generarReporteVentas(List<Venta> ventas, String nombreSucursal, String numeroCaja, String usuario)
            throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // 1. Configuración del Documento (Horizontal/Landscape)
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);
            document.open();

            // 2. Encabezado
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.DARK_GRAY);

            Paragraph titulo = new Paragraph("Reporte de Historial de Ventas", fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            Paragraph info = new Paragraph(
                    "Sucursal: " + nombreSucursal + " | Caja: " + numeroCaja + " | Usuario: " + usuario, fontSubtitulo);
            info.setAlignment(Element.ALIGN_CENTER);
            info.setSpacingAfter(10);
            document.add(info);

            document.add(Chunk.NEWLINE);

            // 3. Tabla de 8 Columnas
            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 1, 2.5f, 2, 2.5f, 3, 3, 2, 2 });

            // Encabezados de Tabla
            String[] headers = { "N°", "Código", "Tipo", "Fecha", "Cliente", "Vendedor", "Total", "Estado" };
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, fontHeader));
                cell.setBackgroundColor(Color.DARK_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // Datos
            Font fontData = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            BigDecimal totalRecaudado = BigDecimal.ZERO;
            int contador = 1;

            for (Venta venta : ventas) {
                agregarCelda(table, String.valueOf(contador++), fontData);
                agregarCelda(table, venta.getNumeroVenta(), fontData);
                agregarCelda(table, venta.getTipoComprobante(), fontData);
                agregarCelda(table, venta.getFecha().format(formatter), fontData);
                agregarCelda(table, venta.getCliente().getNombres() + " " + venta.getCliente().getApellidos(),
                        fontData);
                agregarCelda(table, venta.getEmpleado().getNombre(), fontData);
                // correctos
                agregarCelda(table, "S/ " + venta.getTotal().toString(), fontData);
                agregarCelda(table, venta.getEstado(), fontData);

                // Sumar al total si la venta está confirmada (asumiendo lógica simple)
                if ("confirmada".equalsIgnoreCase(venta.getEstado())) {
                    totalRecaudado = totalRecaudado.add(venta.getTotal());
                }
            }

            document.add(table);

            // 4. Pie de Página
            document.add(Chunk.NEWLINE);
            Paragraph footer = new Paragraph("Total Recaudado (Confirmadas): S/ " + totalRecaudado.toString(),
                    fontTitulo);
            footer.setAlignment(Element.ALIGN_RIGHT);
            document.add(footer);

            document.close();
            return out.toByteArray();
        }
    }

    private void agregarCelda(PdfPTable table, String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }
}
