package com.example.security;

import com.example.dao.UsuariosDao;
import com.example.domain.UserAuth;
import com.example.exceptions.InvalidCredentialsException;
import com.example.exceptions.UserInactiveException;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService{

    @Autowired
    private UsuariosDao userDao;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAuth usuarios = userDao.findByUsername(username);
        if(usuarios == null){
            throw new InvalidCredentialsException("Usuario no encontrado");
        }
        if(!usuarios.isActivo()){
            throw new UserInactiveException("El usuario está inactivo");
        }
        return new CustomUserDetails(
                usuarios.getId(),
                usuarios.getUsername(),
                usuarios.getPassword(),
                usuarios.isActivo(),
                Collections.singletonList(new SimpleGrantedAuthority(usuarios.getPermisos())));
    }
    
}
