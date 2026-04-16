package com.example.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Usuario_tab {
    
    private int id;
    private String nombre;
    private String telefono;
    private String username;
    private String permisos;
    private String estado;
    
}