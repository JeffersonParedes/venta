package pe.edu.promart.sistemaventas.service;

import pe.edu.promart.sistemaventas.dto.ProductoBusquedaDto;

import java.util.List;

// 🔑 CORRECCIÓN: Cambiar 'class' por 'interface'
public interface ProductoService {

    /**
     * Busca productos por coincidencia en código o nombre.
     * * @param termino Palabra clave de búsqueda.
     * 
     * @return Lista de ProductoBusquedaDto.
     */
    List<ProductoBusquedaDto> buscarProductos(String termino);
}
