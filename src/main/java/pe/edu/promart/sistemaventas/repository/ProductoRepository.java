package pe.edu.promart.sistemaventas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.promart.sistemaventas.entity.Producto;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    /**
     * Busca productos donde el código o el nombre contengan el término (ignorando
     * mayúsculas/minúsculas).
     */
    List<Producto> findByCodigoContainingIgnoreCaseOrNombreContainingIgnoreCase(String codigo, String nombre);
}
