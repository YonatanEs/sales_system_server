package com.example.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DtoFiltroHistorialDevolucion {
    
    private String window;
    private String variableWindow;
    
    private String buscador;
    private String busqueda;

    private int page;
    private int size;
    
}
