package com.example.DTO;

import com.example.domain.DetallesVenta;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemProductosDevoluciones {
    
    private Long idProducto;
    private Long idDetalleVenta;
    private String codigo;
    private String descripcion;
    private BigDecimal cantidad;
    private BigDecimal precio;
    private BigDecimal cantidadadevolver;
    private DetallesVenta detallesVenta;
    
    
    public ItemProductosDevoluciones(DetallesVenta productosvendidos){
        this.idProducto = productosvendidos.getIdProducto();
        this.codigo = productosvendidos.getCodigo();
        this.descripcion = productosvendidos.getDescripcion();
        this.cantidad = productosvendidos.getCantidad();
        this.precio = productosvendidos.getPrecioFinal();
        this.cantidadadevolver = BigDecimal.ZERO;
        this.detallesVenta=productosvendidos;
    }
    
}
