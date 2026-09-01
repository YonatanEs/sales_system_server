package com.example.DTO;

import com.example.domain.DetallesVenta;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DtoRegistrarDevolucion {
    
    private Long idVenta;
    private Long idVendedor;
    private Long idTurno;
    private String motivo;
    private List<ItemProductosDevoluciones> productosDevueltos;
    
}
