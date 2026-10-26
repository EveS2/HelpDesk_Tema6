package br.edu.helpdesk.exception;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
import java.time.Instant;
import java.util.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    public record Erro(Instant quando, int status, String mensagem) {}
    @ExceptionHandler(RegraException.class)
    public ResponseEntity<Erro> regra(RegraException e) {
        return resposta(e.status, e.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Erro> validacao(MethodArgumentNotValidException e) {
        String campo = e.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .findFirst().orElse("Dados invalidos.");
        return resposta(HttpStatus.BAD_REQUEST, campo);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Erro> jsonInvalido(Exception e) {
        return resposta(HttpStatus.BAD_REQUEST, "JSON invalido ou valor de enum desconhecido.");
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Erro> integridade(Exception e) {
        return resposta(HttpStatus.CONFLICT, "Registro duplicado ou referenciado por outros dados.");
    }
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Erro> acesso(Exception e) {
        return resposta(HttpStatus.FORBIDDEN, "Acesso negado.");
    }
    private ResponseEntity<Erro> resposta(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(new Erro(Instant.now(), status.value(), mensagem));
    }
}
