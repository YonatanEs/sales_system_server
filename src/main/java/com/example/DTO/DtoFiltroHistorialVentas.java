package com.example.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DtoFiltroHistorialVentas {

    public enum ContextoOrigen {
        GENERAL,
        CLIENTE,
        TURNO
    }

    public enum ContextoBusqueda{
        buscadornit,
        buscadornorecibo,
        buscadorfecha
    }
    
    private ContextoOrigen contexto;
    private ContextoBusqueda contextoBusqueda;
    private String busqueda;
    private String winParametro;

    private String fecha;
    private int pageSize;
    private int page;

}
