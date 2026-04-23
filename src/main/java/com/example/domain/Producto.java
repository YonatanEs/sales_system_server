package com.example.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;


@Entity
@Data
@Table(name="productos")
public class Producto {

    private Long id;
    private String codigo;
    private String descripcion;
    private double precio;
    private double precio_por_mayor;
    private int id_categoria; 
    private int id_medida;
    private int id_proveedor;
    private String estado;
    
}
