package pe.edu.promart.sistemaventas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.promart.sistemaventas.entity.Caja;

import java.util.List;

@Repository
public interface CajaRepository extends JpaRepository<Caja, Integer> {
    List<Caja> findBySucursalIdSucursalAndEstado(Integer idSucursal, String estado);
}
