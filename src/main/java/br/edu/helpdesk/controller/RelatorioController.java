package br.edu.helpdesk.controller;

import br.edu.helpdesk.dto.Dto;
import br.edu.helpdesk.service.RelatorioService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/relatorios")
@PreAuthorize("hasRole('SUPERVISOR')")
public class RelatorioController {
    private final RelatorioService service;
    public RelatorioController(RelatorioService service) { this.service = service; }
    @GetMapping("/prazos")
    public List<Dto.AlertaPrazo> prazos() { return service.alertas(); }
    @GetMapping("/tempos")
    public Dto.RelatorioMedias tempos() { return service.tempos(); }
    @GetMapping("/notas")
    public List<Dto.MediaNota> notas() { return service.notas(); }
}
