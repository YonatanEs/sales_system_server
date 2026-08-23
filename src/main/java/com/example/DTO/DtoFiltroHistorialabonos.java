package com.example.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DtoFiltroHistorialabonos {
    
    private Long idCliente;
    private String contextoBusqueda;
    private String contentBusqueda;
    private int page;
    private int size;

}
