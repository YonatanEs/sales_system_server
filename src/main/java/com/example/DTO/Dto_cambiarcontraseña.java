package com.example.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Dto_cambiarcontraseña {

    private Long idUserAuth;
    private String passwordActual;
    private String passwordNuevo;
    
}
