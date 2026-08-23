package com.example.Repository;

import com.example.domain.DetallesVenta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


public interface DetallesVentaRepository extends JpaRepository<DetallesVenta, Long>{
    
    List<DetallesVenta> findByIdVenta(Long idVenta);
            
}
