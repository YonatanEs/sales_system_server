package com.example.dao;

import com.example.domain.Cliente;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long>{
    
    boolean existsByNit(String nit);
    boolean existsByNitAndIdNot(String nit, Long id);
    List<Cliente> findByEstado(String estado);
    List<Cliente> findAllByOrderByEstadoAsc();
    List<Cliente> findByNombreContainingIgnoreCase(String nombre);
    List<Cliente> findByNombreContainingIgnoreCaseOrNitContainingIgnoreCase(String nombre, String nit);
    
}
