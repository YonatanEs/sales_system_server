package com.example.Repository;

import com.example.domain.Devolucion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DevolucionRepository extends JpaRepository<Devolucion, Long>{
    List<Devolucion> findByIdVenta(Long idVenta);
}
