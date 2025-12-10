package pe.edu.promart.sistemaventas.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.edu.promart.sistemaventas.dto.ProductoBusquedaDto;
import pe.edu.promart.sistemaventas.entity.Producto;
import pe.edu.promart.sistemaventas.repository.ProductoRepository;
import pe.edu.promart.sistemaventas.service.ProductoService;

import java.util.List;
import java.util.stream.Collectors;
import static pe.edu.promart.sistemaventas.dto.ProductoBusquedaDto.builder; // Ya está aquí

@Service
public class ProductoServiceImpl implements ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Override
    public List<ProductoBusquedaDto> buscarProductos(String termino) {

        // CORRECCIÓN: No agregar '%' manualmente. Spring Data JPA ya lo maneja con
        // 'Containing'.
        List<Producto> productos = productoRepository
                .findByCodigoContainingIgnoreCaseOrNombreContainingIgnoreCase(termino, termino);

        // Mapear de Entity a DTO
        return productos.stream()
                .map(p -> {
                    // Usamos el builder importado estáticamente
                    ProductoBusquedaDto.ProductoBusquedaDtoBuilder dtoBuilder = builder();

                    return dtoBuilder
                            // 🔑 CORRECCIÓN: CONVERTIMOS EXPLICITAMENTE el Integer (Entidad) a Long (DTO)
                            .id(Long.valueOf(p.getIdProducto()))
                            .codigo(p.getCodigo())
                            .nombre(p.getNombre())
                            .precioVenta(p.getPrecio())
                            .stock(p.getStockActual())
                            .build();
                })
                .collect(Collectors.toList());
    }
}