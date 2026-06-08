package com.example.Repository;

import com.example.domain.Caja;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CajaRepository extends JpaRepository<Caja, Long>{

    List<Caja> findAllByOrderByEstadoAsc();

    List<Caja> findByNombreContainingIgnoreCaseOrderByEstado(String valor);

    boolean existsByNombre(String nombre);

    boolean existsByNombreAndIdNot(String nombre, Long id);

    @Query("SELECT c.nombre FROM Caja c ")
    List<String> listarSugerenciasNombres();
    
}
