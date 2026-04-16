package com.example.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.Serializable;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Entity
@Table(name = "users")
public class UserAuth implements Serializable{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String telefono;
    private String username;
    private String password;
    private String permisos;
    private String estado;
    
    @Transient
    private boolean activo;
    
    public boolean isActivo(){
        return "Activo".equalsIgnoreCase(this.estado);
    }
    
    public void setActivo(boolean activo){
        this.activo = activo;
        this.estado = activo ? "Activo" : "Inactivo";
    }
    
}
