package com.example.DTO;

import com.example.domain.Devolucion;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DtoHistorialDevoluciones {
    
    private Long id;
    private Long idVenta;
    private LocalDateTime fecha;
    private String nitCliente;
    private String nombreCliente;
    private BigDecimal totalDevuelto;
    private String tipo;
    
    public DtoHistorialDevoluciones(Devolucion devolucion){
        this.id=devolucion.getId();
        this.idVenta=devolucion.getIdVenta();
        this.fecha=devolucion.getFecha();
        this.nitCliente=devolucion.getNitCliente();
        this.nombreCliente=devolucion.getNombreCliente();
        this.totalDevuelto=devolucion.getTotalDevuelto();
        this.tipo=devolucion.getTipo();
    }
    
}
