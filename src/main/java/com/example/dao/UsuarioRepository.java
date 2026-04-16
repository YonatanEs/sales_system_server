package com.example.dao;

import com.example.domain.Usuario;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByUsername(String username);
    boolean existsByUsernameAndIdNot(String username, Long id);
    long countByPermisos(String permisos);
    List<Usuario> findByEstado(String estado);
    List<Usuario> findAllByOrderByEstadoAsc();
    List<Usuario> findByUsernameContainingIgnoreCase(String username);
    public List<Usuario> findByNombreContainingIgnoreCaseOrUsernameContainingIgnoreCase(String nombre, String username);
    
}
