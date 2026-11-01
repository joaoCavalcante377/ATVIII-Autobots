package com.autobots.automanager.controles;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Respostas de erro no formato application/problem+json. */
@RestControllerAdvice
public class ErrosApi extends ResponseEntityExceptionHandler {
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> conflito(DataIntegrityViolationException ex, WebRequest request) {
        var problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Conflito de dados: verifique valores unicos (documento, placa, identificacao, credencial) e vinculos.");
        return handleExceptionInternal(ex, problema, new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var problema = ProblemDetail.forStatusAndDetail(status, "Dados invalidos.");
        problema.setProperty("campos", ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage()).sorted().toList());
        return handleExceptionInternal(ex, problema, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var problema = ProblemDetail.forStatusAndDetail(status, "Dados invalidos.");
        return handleExceptionInternal(ex, problema, headers, status, request);
    }
}
