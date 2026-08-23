package com.example.DTO;

import com.example.domain.Usuario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Usuario_tab {
    
    private Long id;
    private String nombre;
    private String telefono;
    private String username;
    private String permisos;
    private String estado;
    
    public Usuario_tab(Usuario usuario){
        this.id=usuario.getId();
        this.nombre=usuario.getNombre();
        this.telefono=usuario.getTelefono();
        this.username=usuario.getUsername();
        this.permisos=usuario.getPermisos();
        this.estado=usuario.getEstado();
    }
}