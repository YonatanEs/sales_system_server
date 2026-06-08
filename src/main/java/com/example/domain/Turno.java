package com.example.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name="turnos")
public class Turno {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idCaja")
    private Caja caja;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idUserMaster")
    private Usuario userMaster;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idUserCierre")
    private Usuario userCierre;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;
    private BigDecimal saldoInicial;
    private BigDecimal ingresos = BigDecimal.ZERO;  
    private BigDecimal salidas = BigDecimal.ZERO;  
    private BigDecimal ventas = BigDecimal.ZERO;
    private BigDecimal ventaDepositos = BigDecimal.ZERO; 
    private BigDecimal cobroCredito = BigDecimal.ZERO;
    private BigDecimal saldoFinal = BigDecimal.ZERO;
    private BigDecimal saldoFaltante = BigDecimal.ZERO;
    private BigDecimal saldoSobrante = BigDecimal.ZERO;
    private BigDecimal arqueo = BigDecimal.ZERO;
    private String estado = "Abierto";
    
}

