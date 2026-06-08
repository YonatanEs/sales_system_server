package com.example.DTO;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ModificarProducto {
    private Long id;
    private String codigo;
    private String descripcion;
    private BigDecimal precioVenta;
    private Long id_proveedor;
    private Long id_medida;
    private Long id_categoria;
}
