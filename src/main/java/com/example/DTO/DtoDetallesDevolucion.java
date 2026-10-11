package com.example.DTO;

import com.example.domain.DetallesDevolucion;
import java.math.RoundingMode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DtoDetallesDevolucion {
    
    private String codigo;
    private String descripcion;
    private String precio;
    private String cantidad;
    private String subtotal;
    
    public DtoDetallesDevolucion(DetallesDevolucion detalles){
        this.codigo=detalles.getCodigo();
        this.descripcion=detalles.getDescripcion();
        this.precio="Q "+detalles.getPrecioFinal().setScale(2, RoundingMode.HALF_UP).toPlainString();
        this.cantidad= detalles.getCantidadDevuelta().setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString();
        this.subtotal="Q "+detalles.getSubtotalDevuelto().setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
    
}
