package br.edu.helpdesk.controller;

import br.edu.helpdesk.dto.Dto;
import br.edu.helpdesk.model.Prioridade;
import br.edu.helpdesk.service.ConfiguracaoService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/config")
public class ConfiguracaoController {
    private final ConfiguracaoService config;
    public ConfiguracaoController(ConfiguracaoService config) { this.config = config; }
    @PostMapping("/equipes") @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<Dto.EquipeSaida> equipe(@Valid @RequestBody Dto.EquipeEntrada d) {
        return ResponseEntity.status(201).body(config.criarEquipe(d));
    }
    @GetMapping("/equipes")
    public List<Dto.EquipeSaida> equipes() { return config.listarEquipes(); }
    @PostMapping("/categorias") @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<Dto.CategoriaSaida> categoria(@Valid @RequestBody Dto.CategoriaEntrada d) {
        return ResponseEntity.status(201).body(config.criarCategoria(d));
    }
    @GetMapping("/categorias")
    public List<Dto.CategoriaSaida> categorias() { return config.listarCategorias(); }
    @PostMapping("/tecnicos") @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<Dto.UsuarioSaida> tecnico(@Valid @RequestBody Dto.TecnicoEntrada d) {
        return ResponseEntity.status(201).body(config.criarTecnico(d));
    }
    @PutMapping("/prazos/{prioridade}") @PreAuthorize("hasRole('SUPERVISOR')")
    public Dto.PrazoSaida prazo(@PathVariable Prioridade prioridade, @Valid @RequestBody Dto.PrazoEntrada d) {
        return config.alterarPrazo(prioridade, d);
    }
    @GetMapping("/prazos")
    public List<Dto.PrazoSaida> prazos() { return config.listarPrazos(); }
}
