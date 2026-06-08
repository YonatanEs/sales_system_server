package com.example.DTO;

import com.example.domain.Caja;
import com.example.domain.Usuario;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegistrarTurno {

    private Caja caja;
    private Usuario userMaster;
    private BigDecimal saldoInicial;
    
}