package pe.edu.promart.sistemaventas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.promart.sistemaventas.entity.Venta;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer> {

    @Query("SELECT v FROM Venta v WHERE v.caja.idCaja = :idCaja " +
            "AND v.fecha BETWEEN :fechaInicio AND :fechaFin " +
            "ORDER BY v.fecha DESC")
    List<Venta> buscarHistorialPorCaja(@Param("idCaja") Integer idCaja,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);
}
