package com.example.dao;

import com.example.domain.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuariosDao extends JpaRepository<UserAuth, Long> {
    UserAuth findByUsername(String username);
}
