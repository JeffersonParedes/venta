package pe.edu.promart.sistemaventas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.promart.sistemaventas.entity.Sucursal;

@Repository
public interface SucursalRepository extends JpaRepository<Sucursal, Integer> {
}
