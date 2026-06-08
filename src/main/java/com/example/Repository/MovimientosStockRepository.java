package com.example.Repository;

import com.example.domain.MovimientoStock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientosStockRepository extends JpaRepository<MovimientoStock, Long>{
    List<MovimientoStock> findByTipoMovimientoAndFechaBetweenOrderByFechaDesc(
        String tipo, LocalDateTime inicio, LocalDateTime fin
    );
}
