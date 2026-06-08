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
@Data
@Table(name = "movimientos_stock")
public class MovimientoStock {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "id_producto")
    private Long idProducto;
    
    @Column(name = "id_lote")
    private Long idLote;
    
    @Column(name = "tipo_movimiento")
    private String tipoMovimiento;
    
    private BigDecimal cantidad;
    private BigDecimal costo;
    private String concepto;
    private LocalDate fecha;
    
}
