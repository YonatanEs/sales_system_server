package com.example.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegistrarUsuario {
    
    private String nombre;
    private String telefono;
    private String username;
    private String password;
    private String permisos;
    
}
