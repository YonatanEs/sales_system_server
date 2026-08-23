package com.example.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter 
@Setter 
public class DtoClientes {
    
    private Long id;
    private String nombre;
    private String nit;
    private String telefono;
    private String direccion;
    private String credito;
    private String estado;
    
}
