package com.example.exceptions;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), "Invalid credentials provided");
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserInactiveException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ErrorResponse> handleUserInactiveException(UserInactiveException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), "User is inactive");
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    // Manejo global de errores de conexión a la base de datos
    @ExceptionHandler({DataAccessException.class, ConnectException.class})
    public ResponseEntity<ErrorResponse> handleDatabaseConnectionError(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "No se pudo conectar a la base de datos",
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    // Manejo genérico de cualquier otra excepción
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "Error inesperado en el servidor",
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({java.net.SocketTimeoutException.class})
    public ResponseEntity<ErrorResponse> handleTimeout(SocketTimeoutException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "Tiempo de espera agotado al conectar con la base de datos",
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

}
