package com.example.Repository;

import com.example.domain.Credito;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CreditoRepository extends JpaRepository<Credito, Long> {

    boolean existsByIdClienteAndEstado(Long idCliente, String estadoCredito);

    List<Credito> findByIdClienteAndEstadoNot(Long idCliente, String estado);

    @Query("SELECT c FROM Credito c WHERE c.idCliente = :idCliente "
            + "ORDER BY CASE c.estado "
            + "   WHEN 'VENCIDO' THEN 1 "
            + "   WHEN 'PENDIENTE' THEN 2 "
            + "   WHEN 'PAGADO' THEN 3 "
            + "   ELSE 4 END")
    Page<Credito> findByIdClienteOrderByEstado(@Param("idCliente") Long idCliente, Pageable pageable);

    @Query("SELECT c.idCliente, "
            + "CASE "
            + " WHEN SUM(CASE WHEN c.estado = 'VENCIDO' THEN 1 ELSE 0 END) > 0 THEN 'VENCIDO' "
            + " WHEN SUM(CASE WHEN c.estado = 'PENDIENTE' THEN 1 ELSE 0 END) > 0 THEN 'PENDIENTE' "
            + " ELSE 'AL DIA' END "
            + "FROM Credito c "
            + "WHERE c.estado <> 'PAGADO' AND c.idCliente IN :idsClientes "
            + "GROUP BY c.idCliente")
    List<Object[]> calcularEstadosClientes(@Param("idsClientes") List<Long> idsClientes);
}
