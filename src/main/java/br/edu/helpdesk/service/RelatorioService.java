package br.edu.helpdesk.service;

import br.edu.helpdesk.dto.Dto;
import br.edu.helpdesk.exception.RegraException;
import br.edu.helpdesk.model.*;
import br.edu.helpdesk.repository.ChamadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
import java.util.stream.*;

@Service
public class RelatorioService {
    private final ChamadoRepository chamados;
    private final AuthService auth;
    private final Clock relogio;
    public RelatorioService(ChamadoRepository chamados, AuthService auth, Clock relogio) {
        this.chamados = chamados; this.auth = auth; this.relogio = relogio;
    }
    private void autorizado() {
        if (auth.atual().perfil != Perfil.SUPERVISOR) throw RegraException.proibido();
    }
    @Transactional(readOnly = true)
    public List<Dto.AlertaPrazo> alertas() {
        autorizado();
        Instant agora = relogio.instant();
        return chamados.findAll().stream()
                .filter(c -> c.status == StatusChamado.ABERTO || c.status == StatusChamado.EM_ATENDIMENTO)
                .filter(c -> !c.prazoEm.isAfter(agora.plus(Duration.ofHours(2))))
                .map(c -> new Dto.AlertaPrazo(c.id, c.prazoEm,
                        !c.prazoEm.isAfter(agora) ? "ESTOURADO" : "VENCE_EM_ATE_2H", c.equipe.id))
                .sorted(Comparator.comparing(Dto.AlertaPrazo::prazo)).toList();
    }
    private List<Dto.MediaTempo> agregar(List<Chamado> resolvidos, boolean equipe) {
        Map<String, List<Chamado>> grupos = resolvidos.stream().collect(Collectors.groupingBy(
                c -> equipe ? c.equipe.nome : c.categoria.nome));
        return grupos.entrySet().stream().map(e -> new Dto.MediaTempo(e.getKey(), e.getValue().size(),
                e.getValue().stream().mapToLong(c -> c.segundosAteResolucao).average().orElse(0) / 3600.0))
                .sorted(Comparator.comparing(Dto.MediaTempo::grupo)).toList();
    }
    @Transactional(readOnly = true)
    public Dto.RelatorioMedias tempos() {
        autorizado();
        List<Chamado> resolvidos = chamados.findAll().stream()
                .filter(c -> c.segundosAteResolucao != null).toList();
        return new Dto.RelatorioMedias(agregar(resolvidos, true), agregar(resolvidos, false));
    }
    @Transactional(readOnly = true)
    public List<Dto.MediaNota> notas() {
        autorizado();
        Map<Long, List<Chamado>> porTecnico = chamados.findAll().stream()
                .filter(c -> c.nota != null && c.tecnico != null)
                .collect(Collectors.groupingBy(c -> c.tecnico.id));
        return porTecnico.entrySet().stream().map(e -> new Dto.MediaNota(e.getKey(),
                e.getValue().get(0).tecnico.nome, e.getValue().size(),
                e.getValue().stream().mapToInt(c -> c.nota).average().orElse(0)))
                .sorted(Comparator.comparing(Dto.MediaNota::tecnicoNome)).toList();
    }
}
