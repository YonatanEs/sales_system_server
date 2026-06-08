package com.example.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Table(name = "lotes_stock")
@Data
public class LoteStock {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "id_producto")
    private Long idProducto;
    
    @Column(name = "fecha_entrada")
    private LocalDate fechaEntrada;
    
    @Column(name = "precio_compra")
    private BigDecimal precioCompra;
    
    @Column(name = "stock_inicial")
    private BigDecimal stockInicial;
    
    @Column(name = "stock_actual")
    private BigDecimal stockActual;
    
    private String estado;
    
}
