package com.example.Repository;

import com.example.domain.UserTurnos;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTurnoRepositoriy extends JpaRepository<UserTurnos, Long>{
    boolean existsByIdTurnoAndIdUsuario(Long idTurno, Long idUsuario);
}
