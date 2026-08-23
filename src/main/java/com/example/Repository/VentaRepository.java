package com.example.Repository;

import com.example.domain.Venta;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    Page<Venta> findById(Long id, Pageable pageable);
    
    Page<Venta> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin, Pageable pageable);

    Page<Venta> findByIdTurno(Long idTurno,Pageable pageable);
    
    Page<Venta> findByIdTurnoAndId(Long idTurno,Long id,Pageable pageable);
    
    Page<Venta> findByNombreClienteContainingIgnoreCaseOrNitClienteContainingIgnoreCase(String nombreCliente,
            String nitCliente, Pageable pageable);

    Page<Venta> findByIdTurnoAndNombreClienteContainingIgnoreCaseOrIdTurnoAndNitClienteContainingIgnoreCase(
            Long idTurno1, String busquedaNombre,
            Long idTurno2, String busquedaNit,
            Pageable pageable
    );
    
    Page<Venta> findByIdTurnoAndFechaBetween(Long idTurno, LocalDateTime inicio,
            LocalDateTime fin, Pageable pageable);

    Page<Venta> findByNitCliente(String nitCliente, Pageable pageable);
    
    Page<Venta> findByNitClienteAndId(String nitCliente, Long id, Pageable pageable);
    
    Page<Venta> findByNitClienteAndFechaBetween(String nitCliente, LocalDateTime inicio,
            LocalDateTime fin, Pageable pageable);
   
}
