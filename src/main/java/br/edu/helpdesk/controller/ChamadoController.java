package br.edu.helpdesk.controller;

import br.edu.helpdesk.dto.Dto;
import br.edu.helpdesk.service.ChamadoService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/chamados")
public class ChamadoController {
    private final ChamadoService servico;
    public ChamadoController(ChamadoService servico) { this.servico = servico; }
    @PostMapping @PreAuthorize("hasRole('SOLICITANTE')")
    public ResponseEntity<Dto.ChamadoSaida> abrir(@Valid @RequestBody Dto.ChamadoEntrada d) {
        return ResponseEntity.status(201).body(servico.abrir(d));
    }
    @GetMapping
    public List<Dto.ChamadoSaida> listar() { return servico.listar(); }
    @GetMapping("/{id}")
    public Dto.ChamadoSaida detalhe(@PathVariable Long id) { return servico.detalhar(id); }
    @PostMapping("/{id}/assumir") @PreAuthorize("hasRole('TECNICO')")
    public Dto.ChamadoSaida assumir(@PathVariable Long id) { return servico.assumir(id); }
    @PostMapping("/{id}/comentarios") @PreAuthorize("hasAnyRole('SOLICITANTE','TECNICO')")
    public ResponseEntity<Dto.ComentarioSaida> comentar(@PathVariable Long id,
            @Valid @RequestBody Dto.ComentarioEntrada d) {
        return ResponseEntity.status(201).body(servico.comentar(id, d));
    }
    @GetMapping("/{id}/comentarios")
    public List<Dto.ComentarioSaida> comentarios(@PathVariable Long id) {
        return servico.listarComentarios(id);
    }
    @GetMapping("/{id}/historico")
    public List<Dto.HistoricoSaida> historico(@PathVariable Long id) {
        return servico.listarHistorico(id);
    }
    @PostMapping("/{id}/aguardar") @PreAuthorize("hasRole('TECNICO')")
    public Dto.ChamadoSaida aguardar(@PathVariable Long id) { return servico.aguardar(id); }
    @PostMapping("/{id}/transferir") @PreAuthorize("hasRole('TECNICO')")
    public Dto.ChamadoSaida transferir(@PathVariable Long id,
            @Valid @RequestBody Dto.TransferenciaEntrada d) { return servico.transferir(id, d); }
    @PostMapping("/{id}/resolver") @PreAuthorize("hasRole('TECNICO')")
    public Dto.ChamadoSaida resolver(@PathVariable Long id,
            @Valid @RequestBody Dto.TextoEntrada d) { return servico.resolver(id, d); }
    @PostMapping("/{id}/fechar") @PreAuthorize("hasRole('SOLICITANTE')")
    public Dto.ChamadoSaida fechar(@PathVariable Long id,
            @Valid @RequestBody Dto.FechamentoEntrada d) { return servico.fechar(id, d); }
    @PostMapping("/{id}/reabrir") @PreAuthorize("hasRole('SOLICITANTE')")
    public Dto.ChamadoSaida reabrir(@PathVariable Long id) { return servico.reabrir(id); }
    @PatchMapping("/{id}/prioridade") @PreAuthorize("hasRole('SUPERVISOR')")
    public Dto.ChamadoSaida prioridade(@PathVariable Long id,
            @Valid @RequestBody Dto.PrioridadeEntrada d) { return servico.mudarPrioridade(id, d); }
    @PatchMapping("/{id}/atribuir") @PreAuthorize("hasRole('SUPERVISOR')")
    public Dto.ChamadoSaida atribuir(@PathVariable Long id,
            @Valid @RequestBody Dto.AtribuicaoEntrada d) { return servico.reatribuir(id, d); }
    @PostMapping("/processar-pendentes") @PreAuthorize("hasRole('SUPERVISOR')")
    public Dto.ProcessamentoSaida processar() { return servico.processar(); }
}
