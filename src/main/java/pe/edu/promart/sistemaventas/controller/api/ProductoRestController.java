package pe.edu.promart.sistemaventas.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pe.edu.promart.sistemaventas.dto.ProductoBusquedaDto;
import pe.edu.promart.sistemaventas.service.ProductoService;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoRestController {

    @Autowired
    private ProductoService productoService;

    /**
     * Busca productos por coincidencia en nombre o código.
     * 
     * @param term Palabra clave de búsqueda.
     * @return Lista de ProductoBusquedaDto.
     */
    @GetMapping("/buscar")
    public List<ProductoBusquedaDto> buscarProductos(@RequestParam String term) {
        // Llama al servicio para ejecutar la búsqueda
        return productoService.buscarProductos(term);
    }
}
