package br.edu.helpdesk.controller;

import br.edu.helpdesk.dto.Dto;
import br.edu.helpdesk.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }
    @PostMapping("/cadastro")
    public ResponseEntity<Dto.UsuarioSaida> cadastrar(@Valid @RequestBody Dto.Cadastro dados) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.cadastrar(dados));
    }
    @PostMapping("/login")
    public Dto.Token login(@Valid @RequestBody Dto.Login dados) {
        return auth.entrar(dados);
    }
}
