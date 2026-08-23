package com.example.Repository;

import com.example.domain.Abono;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AbonoRepository extends JpaRepository<Abono, Long>{
    Page<Abono> findByIdClienteOrderByFechaAbonoDesc(Long idCliente, Pageable pageable);

    Page<Abono> findByIdClienteAndIdOrderByFechaAbonoDesc(Long idCliente, Long idAbono, Pageable pageable);

    Page<Abono> findByIdClienteAndIdVentaOrderByFechaAbonoDesc(Long idCliente, Long idVenta, Pageable pageable);
}
