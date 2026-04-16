package com.example.controllerRest;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
public class LogOutController {
    
    private final List<String> invalitedTokens = new ArrayList<>();
    
     @PostMapping("/api/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, @RequestHeader("Authorization") String token){
        log.info("Cerrando session");
        log.info(token);
        if (token.startsWith("Bearer ")) {
            token = token.substring(7); // Elimina el prefijo "Bearer "
        }
        invalitedTokens.add(token);
        
        return ResponseEntity.ok("Session cerrada exitosamente");
    }
    
    public boolean IsTokenInvalited(String token){
        return    invalitedTokens.contains(token);
    }
}
