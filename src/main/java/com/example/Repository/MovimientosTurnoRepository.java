package com.example.Repository;

import com.example.domain.MovimientosTurno;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovimientosTurnoRepository extends JpaRepository<MovimientosTurno, Long>{
    
    List<MovimientosTurno> findByIdTurnoOrderByFechaAsc(Long idTurno);
    
    @Query(value = "SELECT * FROM movimientos_de_turno m WHERE "
            + "REPLACE(DATE_FORMAT(m.fecha, '%d/%m/%Y %h:%i %p'), ' ', '') LIKE CONCAT('%', :busqueda, '%') AND m.idTurno=:idTurnoBusqueda "
            + "ORDER BY m.fecha ASC ",
       nativeQuery = true)
    List<MovimientosTurno> buscarPorFechaYIdTurno(@Param("busqueda") String busqueda,@Param("idTurnoBusqueda") Long idTurno);
}
