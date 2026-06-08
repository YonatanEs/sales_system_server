package com.example.Repository;

import com.example.domain.Cliente;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ClienteRepository extends JpaRepository<Cliente, Long>{
    
    boolean existsByNit(String nit);
    boolean existsByNitAndIdNot(String nit, Long id);
    List<Cliente> findByEstado(String estado);
    List<Cliente> findAllByOrderByEstadoAsc();
    List<Cliente> findByNombreContainingIgnoreCase(String nombre);
    List<Cliente> findByNombreContainingIgnoreCaseOrNitContainingIgnoreCase(String nombre, String nit);
    @Query(value = "SELECT nombre FROM clientes "
            + "UNION "
            + "SELECT nit FROM clientes ",
            nativeQuery = true) 
    List<String> listarSugerencias();
}
