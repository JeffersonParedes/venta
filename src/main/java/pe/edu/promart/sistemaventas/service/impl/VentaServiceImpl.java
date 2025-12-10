package pe.edu.promart.sistemaventas.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.promart.sistemaventas.service.VentaService;
import pe.edu.promart.sistemaventas.dto.DetalleVentaDto;
import pe.edu.promart.sistemaventas.dto.VentaRequestDto;
import pe.edu.promart.sistemaventas.dto.VentaResponseDto;
import pe.edu.promart.sistemaventas.entity.*;
import pe.edu.promart.sistemaventas.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VentaServiceImpl implements VentaService {

        @Autowired
        private VentaRepository ventaRepository;
        @Autowired
        private ProductoRepository productoRepository;
        @Autowired
        private UsuarioRepository usuarioRepository;
        @Autowired
        private CajaRepository cajaRepository;
        @Autowired
        private ClienteRepository clienteRepository;
        @Autowired
        private DetalleVentaRepository detalleVentaRepository;

        @Transactional
        public VentaResponseDto registrarVenta(VentaRequestDto dto, String username, Integer idCaja) {

                // --- 1. VALIDACIÓN Y BÚSQUEDA DE ENTIDADES ---

                Usuario usuario = usuarioRepository.findByUsuario(username)
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                Caja caja = cajaRepository.findById(idCaja)
                                .orElseThrow(() -> new RuntimeException("Caja no encontrada"));

                // Validar Cliente (Usamos ID 1 'Público General' si viene null o 0)
                Integer idClienteFinal = (dto.getIdCliente() != null && dto.getIdCliente() > 0) ? dto.getIdCliente()
                                : 1;
                Cliente cliente = clienteRepository.findById(idClienteFinal)
                                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

                // --- 2. CÁLCULO DE TOTALES ---

                // Validar que el totalVenta no sea null
                if (dto.getTotalVenta() == null) {
                        throw new RuntimeException("El total de la venta no puede ser nulo");
                }
                
                // Validar que haya detalles
                if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
                        throw new RuntimeException("La venta debe tener al menos un producto");
                }

                BigDecimal total = dto.getTotalVenta();
                // 1.18 es BigDecimal(1.18)
                BigDecimal subtotalNeto = total.divide(new BigDecimal("1.18"), 2, java.math.RoundingMode.HALF_UP);
                BigDecimal impuestos = total.subtract(subtotalNeto);

                BigDecimal montoRecibido = dto.getMontoRecibido() != null ? dto.getMontoRecibido() : total;
                BigDecimal cambio = dto.getCambio() != null ? dto.getCambio() : BigDecimal.ZERO;
                BigDecimal descuentos = dto.getDescuentos() != null ? dto.getDescuentos() : BigDecimal.ZERO;

                // Validación de monto recibido (solo para métodos que requieren pago inmediato)
                // Para tarjetas, yape, plin, etc., el monto recibido puede ser igual al total
                String metodoPago = dto.getMetodoPago() != null ? dto.getMetodoPago().toUpperCase().replace("_", " ") : "EFECTIVO";
                if ("EFECTIVO".equals(metodoPago) && montoRecibido.compareTo(total) < 0) {
                        throw new RuntimeException("Monto recibido insuficiente para el total de la venta. Total: " + total + ", Recibido: " + montoRecibido);
                }
                
                // Para métodos no efectivo, ajustar cambio a 0 si es necesario
                if (!"EFECTIVO".equals(metodoPago)) {
                        cambio = BigDecimal.ZERO;
                        montoRecibido = total; // El monto recibido es igual al total para métodos no efectivo
                }

                // --- 3. CREACIÓN DE CABECERA VENTA ---

                Venta venta = new Venta();
                venta.setNumeroVenta("VNT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                venta.setFecha(LocalDateTime.now());
                venta.setTipoComprobante(dto.getTipoComprobante() != null ? dto.getTipoComprobante() : "BOLETA");
                // Normalizar método de pago: convertir a mayúsculas y reemplazar guiones bajos
                String metodoPagoNormalizado = dto.getMetodoPago() != null ? 
                    dto.getMetodoPago().toUpperCase().replace("_", " ") : "EFECTIVO";
                venta.setMetodoPago(metodoPagoNormalizado);

                // Asignación de campos
                // Intentar diferentes valores comunes para canal_venta según la restricción CHECK de la BD
                // Valores comunes: "FISICO", "LOCAL", "TIENDA", "ONLINE", "TELEFONO"
                // Si ninguno funciona, el error mostrará un mensaje simplificado al usuario
                venta.setCanalVenta("FISICO"); // Sin la 'A' al final
                venta.setSubtotal(subtotalNeto);
                venta.setImpuestos(impuestos);
                venta.setTotal(total);
                venta.setDescuentos(descuentos);
                venta.setMontoRecibido(montoRecibido);
                venta.setCambio(cambio);
                venta.setEstado("confirmada");
                venta.setObservaciones(dto.getObservaciones());

                // Relaciones
                venta.setCliente(cliente);
                venta.setEmpleado(usuario.getEmpleado());
                venta.setSucursal(usuario.getEmpleado().getSucursal());
                venta.setCaja(caja);

                // 4. Guardar Venta
                Venta ventaGuardada = ventaRepository.save(venta);

                // --- 5. GUARDAR DETALLES Y DESCONTAR STOCK (¡LÓGICA ACTIVADA!) ---

                for (DetalleVentaDto item : dto.getDetalles()) {

                        Producto producto = productoRepository.findById(item.getIdProducto())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Producto ID " + item.getIdProducto() + " no encontrado"));

                        // 🔑 5a. Validación de Stock (ACTIVADA)
                        if (producto.getStockActual() < item.getCantidad()) {
                                throw new RuntimeException("Stock insuficiente para el producto: "
                                                + producto.getNombre()
                                                + " (Stock actual: "
                                                + producto.getStockActual() + ")");
                        }

                        // 5b. Creación de Detalle
                        DetalleVenta detalle = new DetalleVenta();
                        detalle.setVenta(ventaGuardada);
                        detalle.setProducto(producto);
                        detalle.setCantidad(item.getCantidad());
                        detalle.setPrecioUnitario(item.getPrecioUnitario());

                        detalle.setDescuentoLinea(item.getDescuento() != null ? item.getDescuento() : BigDecimal.ZERO);

                        detalle.setSubtotal(item.getSubtotal());
                        detalleVentaRepository.save(detalle);

                        // 🔑 5c. Descuento de Stock (ACTIVADA)
                        Integer nuevaCantidad = producto.getStockActual() - item.getCantidad();
                        producto.setStockActual(nuevaCantidad);
                        productoRepository.save(producto);
                }

                // --- 6. CONSTRUIR RESPUESTA DTO ---

                List<DetalleVentaDto> detalleResumen = ventaGuardada.getDetalleVenta().stream()
                                .map(det -> {
                                        DetalleVentaDto detalleDto = new DetalleVentaDto();
                                        detalleDto.setIdProducto(det.getProducto().getIdProducto());
                                        detalleDto.setNombreProducto(det.getProducto().getNombre());
                                        detalleDto.setProductoCodigo(det.getProducto().getCodigo());
                                        detalleDto.setCantidad(det.getCantidad());
                                        detalleDto.setPrecioUnitario(det.getPrecioUnitario());
                                        detalleDto.setDescuento(det.getDescuentoLinea());
                                        detalleDto.setSubtotal(det.getSubtotal());
                                        return detalleDto;
                                })
                                .collect(Collectors.toList());

                return VentaResponseDto.builder()
                                .nroVenta(ventaGuardada.getNumeroVenta())
                                .idVenta(ventaGuardada.getIdVenta())
                                .tipoComprobante(ventaGuardada.getTipoComprobante())
                                .fecha(ventaGuardada.getFecha())
                                .clienteNombre(cliente.getNombres() + " " + cliente.getApellidos())
                                .metodoPago(ventaGuardada.getMetodoPago())
                                .totalPagado(ventaGuardada.getTotal())
                                .montoRecibido(ventaGuardada.getMontoRecibido())
                                .cambio(ventaGuardada.getCambio())
                                .detalleResumen(detalleResumen)
                                .build();
        }
}