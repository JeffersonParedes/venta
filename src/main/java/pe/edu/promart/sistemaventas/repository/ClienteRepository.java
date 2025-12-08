package pe.edu.promart.sistemaventas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.promart.sistemaventas.entity.Cliente;
import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    // 🔑 MÉTODO AÑADIDO: Búsqueda flexible por documento, nombres o apellidos.
    List<Cliente> findByDocumentoIdentidadContainingIgnoreCaseOrNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(
            String doc, String nombres, String apellidos);
}