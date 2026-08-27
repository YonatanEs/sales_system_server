package com.example.DTO;

import java.util.List;

public class DtoRegistrarDevolucion {
    
    private Long idVenta;
    private Long idVendedor;
    private Long idTurno;
    private String motivo;
    private List<ItemProductosDevoluciones> productosDevueltos;
    
}
