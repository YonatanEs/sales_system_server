package com.example.Repository;

import com.example.domain.LoteStock;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoteStockRepository extends JpaRepository<LoteStock, Long>{
    // ordenados desde el más viejo (primero que entró) al más nuevo.
    List<LoteStock> findByIdProductoAndEstadoAndStockActualGreaterThanOrderByFechaEntradaAsc(Long idProducto, String estado, BigDecimal stock);
    
    Optional<LoteStock> findFirstByIdProductoOrderByIdDesc(Long idProducto);
    
    
}
