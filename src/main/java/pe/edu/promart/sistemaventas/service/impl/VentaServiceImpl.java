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

                BigDecimal total = dto.getTotalVenta();
                // 1.18 es BigDecimal(1.18)
                BigDecimal subtotalNeto = total.divide(new BigDecimal("1.18"), 2, java.math.RoundingMode.HALF_UP);
                BigDecimal impuestos = total.subtract(subtotalNeto);

                BigDecimal montoRecibido = dto.getMontoRecibido() != null ? dto.getMontoRecibido() : total;
                BigDecimal cambio = dto.getCambio() != null ? dto.getCambio() : BigDecimal.ZERO;
                BigDecimal descuentos = dto.getDescuentos() != null ? dto.getDescuentos() : BigDecimal.ZERO;

                // Validación de monto recibido
                if (montoRecibido.compareTo(total) < 0) {
                        throw new RuntimeException("Monto recibido insuficiente para el total de la venta.");
                }

                // --- 3. CREACIÓN DE CABECERA VENTA ---

                Venta venta = new Venta();
                venta.setNumeroVenta("VNT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                venta.setFecha(LocalDateTime.now());
                venta.setTipoComprobante(dto.getTipoComprobante() != null ? dto.getTipoComprobante() : "BOLETA");
                venta.setMetodoPago(dto.getMetodoPago() != null ? dto.getMetodoPago() : "EFECTIVO");

                // Asignación de campos
                venta.setCanalVenta("TIENDA");
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