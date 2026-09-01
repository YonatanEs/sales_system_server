package com.example.Repository;

import com.example.domain.Credito;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CreditoRepository extends JpaRepository<Credito, Long> {

    Optional<Credito> findByIdVenta(Long idVenta);

    boolean existsByIdClienteAndEstado(Long idCliente, String estadoCredito);

    List<Credito> findByIdClienteAndEstadoNot(Long idCliente, String estado);

    @Query("SELECT c FROM Credito c WHERE c.idCliente = :idCliente "
            + "ORDER BY CASE c.estado "
            + "   WHEN 'VENCIDO' THEN 1 "
            + "   WHEN 'PENDIENTE' THEN 2 "
            + "   WHEN 'PAGADO' THEN 3 "
            + "   ELSE 4 END, "
            + "CASE WHEN c.estado = 'PAGADO' THEN c.fechaEmision END DESC")
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

    @Query("SELECT COALESCE(SUM(c.saldoPendiente), 0) "
            + "FROM Credito c "
            + "WHERE c.idCliente = :id AND c.estado <> 'PAGADO'")
    BigDecimal calcularDeudaTotal(@Param("id") Long id);

    @Query("SELECT CASE "
            + "WHEN COUNT(c) > 0 AND EXISTS (SELECT 1 FROM Credito c2 WHERE c2.idCliente = :id AND c2.estado = 'VENCIDO') THEN 'VENCIDO' "
            + "WHEN COUNT(c) > 0 AND EXISTS (SELECT 1 FROM Credito c2 WHERE c2.idCliente = :id AND c2.estado = 'PENDIENTE') THEN 'PENDIENTE' "
            + "ELSE 'AL DIA' END "
            + "FROM Credito c WHERE c.idCliente = :id AND c.estado <> 'PAGADO'")
    String calcularEstadoGeneral(@Param("id") Long id);

    List<Credito> findByEstadoIgnoreCase(String estado);

}
