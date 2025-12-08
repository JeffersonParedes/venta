package pe.edu.promart.sistemaventas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.promart.sistemaventas.entity.AsignacionCaja;

import java.util.Optional;

@Repository
public interface AsignacionCajaRepository extends JpaRepository<AsignacionCaja, Integer> {
    Optional<AsignacionCaja> findByEmpleadoIdEmpleadoAndActivaTrue(Integer idEmpleado);
}
