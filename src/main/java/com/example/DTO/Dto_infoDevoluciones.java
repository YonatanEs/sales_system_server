package com.example.DTO;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Dto_infoDevoluciones {
    
    private Long idVenta;
    private String detalleRecibo1;
    private String detalleRecibo2;
    private String estado;
    private List<ItemProductosDevoluciones> listaProductos;
    
}
