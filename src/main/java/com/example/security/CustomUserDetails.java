package com.example.security;

import com.example.domain.UserAuth;
import com.example.domain.Usuario;
import java.util.Collection;
import java.util.Collections;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserDetails implements UserDetails{

    private Long id;
    private String username;
    private String password;
    private boolean active;
    private Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Long id, String username, String password, boolean active, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.active = active;
        this.authorities = authorities;
    }
    
    public CustomUserDetails(UserAuth usuario){
        this.id = usuario.getId();
        this.username = usuario.getUsername();
        this.password = usuario.getPassword();
        this.active = usuario.isActivo(); // o el campo que uses para habilitar
        this.authorities = Collections.singletonList(
                new SimpleGrantedAuthority(usuario.getPermisos()) // ejemplo: "Administrador" o "Empleado"
        );
    }

    public Long getId(){
        return id;
    }
     
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
    
}
