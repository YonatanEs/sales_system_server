package com.example.Repository;

import com.example.domain.Detallesventa_lotes;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetallesVenta_lotesRepository extends JpaRepository<Detallesventa_lotes, Long>{
    
    List<Detallesventa_lotes> findByIdVentaAndIdProducto(Long idVenta, Long idProducto);
   
}
