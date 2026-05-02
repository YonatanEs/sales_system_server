package com.example.Repository;

import com.example.domain.Producto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Page<Producto> findByCodigoContainingIgnoreCaseOrDescripcionContainingIgnoreCase(
            String codigo, String descripcion, Pageable pageable);

    boolean existsByCodigo(String codigo);

    @Modifying
    @Query("UPDATE Producto p SET p.stock = p.stock + :cantidad WHERE p.id = :id")
    void aumentarStock(@Param("id") Long id, @Param("cantidad") Double cantidad);

    @Query(value = "SELECT codigo FROM productos "
            + "UNION "
            + "SELECT descripcion FROM productos ",
            nativeQuery = true) 
    List<String> listarSugerencias();
}
