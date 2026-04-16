package com.example.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DtoAuthResponse {
    
    private String token;
    private String permisos;
    private String id;
}
