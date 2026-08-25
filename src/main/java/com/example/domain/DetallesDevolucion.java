package com.example.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Data;

@Data
@Entity
@Table(name="detallesdevolucion")
public class DetallesDevolucion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long idDevolucion;
    private Long idProducto;
    
    private String descripcion;
    private String codigo;
    private BigDecimal cantidadDevuelta;
    private BigDecimal precio;
    private BigDecimal descuento;
    private BigDecimal precioFinal;
    private BigDecimal subtotalDevuelto;
    
}
