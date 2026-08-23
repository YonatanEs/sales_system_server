package com.example.DTO;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DtoCredito {
    
    private String nombreCliente;
    private String nitCliente;
    private BigDecimal deudaTotal;
    private String estadoGeneral;
    private RespuestaPaginada<DtoItemCredito> listCredito;
    
}
