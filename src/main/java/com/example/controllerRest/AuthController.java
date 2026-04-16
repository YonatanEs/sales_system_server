package com.example.controllerRest;

import com.example.DTO.DtoAuthRequest;
import com.example.DTO.DtoAuthResponse;
import com.example.security.CustomUserDetails;
import com.example.security.CustomUserDetailsService;
import com.example.security.JwtTokenUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
//@RequestMapping("/login")
@Slf4j
public class AuthController {
    
    @Autowired
    private AuthenticationManager authManager;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody DtoAuthRequest authRequest) throws AuthenticationException{
        Authentication authentication = authManager
                .authenticate(new UsernamePasswordAuthenticationToken
        (authRequest.getUsername(), authRequest.getPassword()));
        
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(authRequest.getUsername());
        
        String token = jwtTokenUtil.generateToken(userDetails);
        String roll = userDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .orElse("Empleado");
        
        String idUser = userDetails.getId().toString();
        
        return ResponseEntity.ok(new DtoAuthResponse(token, roll, idUser));
    }
    
    
}


