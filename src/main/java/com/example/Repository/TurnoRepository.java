package com.example.Repository;

import com.example.domain.Turno;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TurnoRepository extends JpaRepository<Turno, Long> {

    Optional<Turno> findByUserMasterIdAndEstado(Long idUserMaster, String estado);

    List<Turno> findByEstado(String estado);

    @Query(value = "SELECT * FROM turnos t WHERE "
            + "REPLACE(DATE_FORMAT(t.fechaApertura, '%d/%m/%Y %h:%i %p'), ' ', '') LIKE CONCAT('%', :busqueda, '%') "
            + "ORDER BY (CASE WHEN t.estado = 'Abierto' THEN 0 ELSE 1 END), t.fechaApertura DESC",
            countQuery = "SELECT COUNT(*) FROM turnos t WHERE "
            + "REPLACE(DATE_FORMAT(t.fechaApertura, '%d/%m/%Y %h:%i %p'), ' ', '') LIKE CONCAT('%', :busqueda, '%')",
            nativeQuery = true)
    Page<Turno> buscarPorFecha(@Param("busqueda") String busqueda, Pageable pageable);

    boolean existsByCajaIdAndEstado(Long idCaja, String estado);

    Turno findByCajaIdAndEstado(Long idCaja, String estado);

    boolean existsByIdAndEstado(Long id, String estado);

    @Query(value = "SELECT * FROM turnos t ORDER BY (CASE WHEN t.estado = 'Abierto' THEN 0 ELSE 1 END), t.fechaApertura DESC",
            countQuery = "SELECT COUNT(*) FROM turnos",
            nativeQuery = true)
    Page<Turno> findAllCustomOrder(Pageable pageable);
}
