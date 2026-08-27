package com.example.Repository;

import com.example.domain.DetallesDevolucion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetallesDevolucionRepository extends JpaRepository<DetallesDevolucion, Long>{
    List<DetallesDevolucion> findByIdDevolucion(Long idDevolucion);
}
