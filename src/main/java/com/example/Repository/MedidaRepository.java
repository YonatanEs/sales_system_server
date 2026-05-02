package com.example.Repository;

import com.example.domain.Medida;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedidaRepository extends JpaRepository<Medida, Long>{
    
    boolean existsByNombre(String nombre);
    boolean existsByAbreviatura(String abreviatura);
    boolean existsByNombreAndIdNot(String nombre, Long id);
    boolean existsByAbreviaturaAndIdNot(String abreviatura, Long id);
}
