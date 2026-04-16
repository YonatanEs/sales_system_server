package com.example.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DtoAuthRequest {
    
    private String username;
    private String password;
    
}
