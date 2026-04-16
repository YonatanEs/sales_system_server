package com.example.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModificarCliente {

    private Long id;
    private String nombre;
    private String nit;
    private String telefono;
    private String correo;
    private String direccion;
    
}
