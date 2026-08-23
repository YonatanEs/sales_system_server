package com.example.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "abonos")
public class Abono {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long idCredito;
    private Long idCliente;
    private Long idVenta;
    
    private LocalDateTime fechaAbono;
   
    private BigDecimal montoAbonado;
    private BigDecimal saldoAnterior;
    private BigDecimal nuevoSaldo;
    
    private String formaPago;
    private String estadoCredito;
    
    private Long idVendedor;
    
    private String noDeposito;
    private String urlDeposito;

}
