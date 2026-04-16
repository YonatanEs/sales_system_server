package com.example.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DtoResponse {
    
    private boolean success;
    private String message;
    
    public DtoResponse(boolean succes, String message){
        this.success=succes;
        this.message=message;
    }
    
}
