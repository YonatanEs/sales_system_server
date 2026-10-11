package com.example.Repository;

import com.example.domain.Devolucion;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DevolucionRepository extends JpaRepository<Devolucion, Long>{
    List<Devolucion> findByIdVenta(Long idVenta);
    
    Page<Devolucion> findByIdVenta(Long idVenta, Pageable pageable);
    
    Page<Devolucion> findById(Long id, Pageable pageable);
    
    Page<Devolucion> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin, Pageable pageable);

    Page<Devolucion> findByIdTurno(Long idTurno,Pageable pageable);
    
    Page<Devolucion> findByIdTurnoAndId(Long idTurno,Long id,Pageable pageable);
    
    Page<Devolucion> findByNombreClienteContainingIgnoreCaseOrNitClienteContainingIgnoreCase(String nombreCliente,
            String nitCliente, Pageable pageable);

    Page<Devolucion> findByIdTurnoAndNombreClienteContainingIgnoreCaseOrIdTurnoAndNitClienteContainingIgnoreCase(
            Long idTurno1, String busquedaNombre,
            Long idTurno2, String busquedaNit,
            Pageable pageable
    );
    
    Page<Devolucion> findByIdTurnoAndFechaBetween(Long idTurno, LocalDateTime inicio,
            LocalDateTime fin, Pageable pageable);

    Page<Devolucion> findByNitCliente(String nitCliente, Pageable pageable);
    
    Page<Devolucion> findByNitClienteAndId(String nitCliente, Long id, Pageable pageable);
    
    Page<Devolucion> findByNitClienteAndFechaBetween(String nitCliente, LocalDateTime inicio,
            LocalDateTime fin, Pageable pageable);
    
}
