package com.example.DTO;

import com.example.domain.Categoria;
import com.example.domain.Medida;
import com.example.domain.Proveedor;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Producto_tab {
    
    private Long id;
    private String codigo;
    private String descripcion;
    private BigDecimal stock;
    private BigDecimal precio_venta;
    private BigDecimal precio_compra;
    private Categoria categoria_ob;
    private Medida medida_ob;
    private Proveedor proveedor_ob;
    private String estado;

}
