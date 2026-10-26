package br.edu.helpdesk.service;

import br.edu.helpdesk.dto.Dto;
import br.edu.helpdesk.exception.RegraException;
import br.edu.helpdesk.model.*;
import br.edu.helpdesk.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service
public class ChamadoService {
    private final ChamadoRepository chamados;
    private final UsuarioRepository usuarios;
    private final CategoriaRepository categorias;
    private final EquipeRepository equipes;
    private final PrazoRepository prazos;
    private final ComentarioRepository comentarios;
    private final HistoricoRepository historicos;
    private final AuthService auth;
    private final Clock relogio;
    public ChamadoService(ChamadoRepository chamados, UsuarioRepository usuarios,
            CategoriaRepository categorias, EquipeRepository equipes, PrazoRepository prazos,
            ComentarioRepository comentarios, HistoricoRepository historicos,
            AuthService auth, Clock relogio) {
        this.chamados = chamados; this.usuarios = usuarios; this.categorias = categorias;
        this.equipes = equipes; this.prazos = prazos; this.comentarios = comentarios;
        this.historicos = historicos; this.auth = auth; this.relogio = relogio;
    }
    private Instant agora() { return relogio.instant(); }
    private Chamado buscar(Long id) {
        return chamados.findById(id).orElseThrow(() -> RegraException.naoEncontrado("Chamado"));
    }
    private Chamado bloquear(Long id) {
        return chamados.bloquear(id).orElseThrow(() -> RegraException.naoEncontrado("Chamado"));
    }
    private void precisa(boolean condicao, String mensagem) {
        if (!condicao) throw RegraException.conflito(mensagem);
    }
    private void podeLer(Usuario u, Chamado c) {
        if (u.perfil == Perfil.SUPERVISOR) return;
        if (u.perfil == Perfil.SOLICITANTE && c.solicitante.id.equals(u.id)) return;
        if (u.perfil == Perfil.TECNICO && u.equipe != null && c.equipe.id.equals(u.equipe.id)) return;
        throw RegraException.proibido();
    }
    private void dono(Usuario u, Chamado c) {
        if (u.perfil != Perfil.SOLICITANTE || !c.solicitante.id.equals(u.id)) throw RegraException.proibido();
    }
    private void tecnicoResponsavel(Usuario u, Chamado c) {
        if (u.perfil != Perfil.TECNICO || u.equipe == null || c.tecnico == null
                || !u.id.equals(c.tecnico.id) || !u.equipe.id.equals(c.equipe.id)) {
            throw RegraException.proibido();
        }
    }
    private void supervisor(Usuario u) {
        if (u.perfil != Perfil.SUPERVISOR) throw RegraException.proibido();
    }
    private void historico(Chamado c, Usuario autor, String campo, Object antes,
                           Object depois, String motivo) {
        historicos.save(new HistoricoChamado(c, autor, agora(), campo,
                String.valueOf(antes), String.valueOf(depois), motivo));
    }
    private void status(Chamado c, Usuario autor, StatusChamado novo, String motivo) {
        StatusChamado antigo = c.status;
        c.status = novo;
        historico(c, autor, "status", antigo, novo, motivo);
    }
    private int horas(Prioridade prioridade) {
        return prazos.findById(prioridade)
                .orElseThrow(() -> RegraException.naoEncontrado("Prazo")).horas;
    }
    private void liberarPausa(Chamado c, Instant instante) {
        if (c.pausaIniciadaEm == null) return;
        long segundos = Math.max(0, Duration.between(c.pausaIniciadaEm, instante).getSeconds());
        c.segundosPausados += segundos;
        c.prazoEm = c.prazoEm.plusSeconds(segundos);
        c.pausaIniciadaEm = null;
    }
    private void limiteCinco(Long tecnicoId) {
        long ativos = chamados.countByTecnicoIdAndStatus(tecnicoId, StatusChamado.EM_ATENDIMENTO);
        precisa(ativos < 5, "Tecnico ja possui 5 chamados em atendimento.");
    }
    @Transactional
    public Dto.ChamadoSaida abrir(Dto.ChamadoEntrada d) {
        Usuario autor = auth.atual();
        if (autor.perfil != Perfil.SOLICITANTE) throw RegraException.proibido();
        Categoria categoria = categorias.findById(d.categoriaId())
                .orElseThrow(() -> RegraException.naoEncontrado("Categoria"));
        Instant agora = agora();
        Chamado c = chamados.save(new Chamado(d.titulo(), d.descricao(), d.prioridade(),
                categoria, autor, agora, RegraDePrazos.limite(agora, horas(d.prioridade()), 0)));
        historico(c, autor, "status", null, c.status, "Abertura");
        historico(c, autor, "equipe", null, c.equipe.id, "Equipe da categoria");
        return Dto.ChamadoSaida.de(c);
    }
    @Transactional(readOnly = true)
    public List<Dto.ChamadoSaida> listar() {
        Usuario u = auth.atual();
        List<Chamado> lista = switch (u.perfil) {
            case SOLICITANTE -> chamados.findBySolicitanteIdOrderByAbertoEmDesc(u.id);
            case TECNICO -> chamados.findByEquipeIdOrderByAbertoEmDesc(u.equipe.id);
            case SUPERVISOR -> chamados.findAll();
        };
        return lista.stream().map(Dto.ChamadoSaida::de).toList();
    }
    @Transactional(readOnly = true)
    public Dto.ChamadoSaida detalhar(Long id) {
        Usuario u = auth.atual();
        Chamado c = buscar(id);
        podeLer(u, c);
        return Dto.ChamadoSaida.de(c);
    }
    @Transactional
    public Dto.ChamadoSaida assumir(Long id) {
        Usuario u = auth.atual();
        if (u.perfil != Perfil.TECNICO || u.equipe == null) throw RegraException.proibido();
        usuarios.bloquear(u.id).orElseThrow(() -> RegraException.naoEncontrado("Tecnico"));
        Chamado c = bloquear(id);
        precisa(c.status == StatusChamado.ABERTO && c.tecnico == null, "Chamado nao esta na fila.");
        if (!c.equipe.id.equals(u.equipe.id)) throw RegraException.proibido();
        limiteCinco(u.id);
        c.tecnico = u;
        historico(c, u, "tecnico", null, u.id, "Chamado assumido");
        status(c, u, StatusChamado.EM_ATENDIMENTO, "Chamado assumido");
        return Dto.ChamadoSaida.de(c);
    }
    @Transactional
    public Dto.ComentarioSaida comentar(Long id, Dto.ComentarioEntrada d) {
        Usuario u = auth.atual();
        Chamado c = bloquear(id);
        podeLer(u, c);
        precisa(c.status == StatusChamado.ABERTO || c.status == StatusChamado.EM_ATENDIMENTO
                || c.status == StatusChamado.AGUARDANDO_SOLICITANTE, "Chamado nao aceita comentarios.");
        if (u.perfil == Perfil.SOLICITANTE) {
            if (d.interno()) throw RegraException.proibido();
            if (c.status == StatusChamado.AGUARDANDO_SOLICITANTE) {
                liberarPausa(c, agora());
                if (c.tecnico != null && chamados.countByTecnicoIdAndStatus(
                        c.tecnico.id, StatusChamado.EM_ATENDIMENTO) >= 5) {
                    Long anterior = c.tecnico.id;
                    c.tecnico = null;
                    historico(c, u, "tecnico", anterior, null, "Fila por limite de 5 ativos");
                    status(c, u, StatusChamado.ABERTO, "Resposta recebida; voltou a fila");
                } else {
                    status(c, u, StatusChamado.EM_ATENDIMENTO, "Solicitante respondeu");
                }
            }
        } else {
            tecnicoResponsavel(u, c);
        }
        Comentario comentario = comentarios.save(new Comentario(c, u, d.texto(), d.interno(), agora()));
        return Dto.ComentarioSaida.de(comentario);
    }
    @Transactional(readOnly = true)
    public List<Dto.ComentarioSaida> listarComentarios(Long id) {
        Usuario u = auth.atual();
        Chamado c = buscar(id);
        podeLer(u, c);
        return comentarios.findByChamadoIdOrderByCriadoEmAsc(id).stream()
                .filter(com -> u.perfil != Perfil.SOLICITANTE || !com.interno)
                .map(Dto.ComentarioSaida::de).toList();
    }
    @Transactional(readOnly = true)
    public List<Dto.HistoricoSaida> listarHistorico(Long id) {
        Usuario u = auth.atual();
        Chamado c = buscar(id);
        podeLer(u, c);
        return historicos.findByChamadoIdOrderByDataAlteracaoAsc(id).stream()
                .map(Dto.HistoricoSaida::de).toList();
    }
    @Transactional
    public Dto.ChamadoSaida aguardar(Long id) {
        Usuario u = auth.atual();
        Chamado c = bloquear(id);
        tecnicoResponsavel(u, c);
        precisa(c.status == StatusChamado.EM_ATENDIMENTO, "Chamado nao esta em atendimento.");
        c.pausaIniciadaEm = agora();
        status(c, u, StatusChamado.AGUARDANDO_SOLICITANTE, "Aguardando resposta");
        return Dto.ChamadoSaida.de(c);
    }
    @Transactional
    public Dto.ChamadoSaida transferir(Long id, Dto.TransferenciaEntrada d) {
        Usuario u = auth.atual();
        Chamado c = bloquear(id);
        tecnicoResponsavel(u, c);
        precisa(c.status == StatusChamado.EM_ATENDIMENTO ||
                c.status == StatusChamado.AGUARDANDO_SOLICITANTE, "Chamado nao pode ser transferido.");
        Equipe destino = equipes.findById(d.equipeId()).orElseThrow(() -> RegraException.naoEncontrado("Equipe"));
        precisa(!destino.id.equals(c.equipe.id), "Escolha outra equipe.");
        liberarPausa(c, agora());
        Long tecnicoAnterior = c.tecnico.id;
        Long equipeAnterior = c.equipe.id;
        c.tecnico = null;
        c.equipe = destino;
        historico(c, u, "tecnico", tecnicoAnterior, null, d.justificativa());
        historico(c, u, "equipe", equipeAnterior, destino.id, d.justificativa());
        status(c, u, StatusChamado.ABERTO, "Transferencia: " + d.justificativa());
        return Dto.ChamadoSaida.de(c);
    }
    @Transactional
    public Dto.ChamadoSaida resolver(Long id, Dto.TextoEntrada d) {
        Usuario u = auth.atual();
        Chamado c = bloquear(id);
        tecnicoResponsavel(u, c);
        precisa(c.status == StatusChamado.EM_ATENDIMENTO, "Chamado nao esta em atendimento.");
        Instant instante = agora();
        c.solucao = d.texto();
        c.resolvidoEm = instante;
        c.segundosAteResolucao = RegraDePrazos.segundosUteis(c.abertoEm, instante, c.segundosPausados);
        status(c, u, StatusChamado.RESOLVIDO, "Solucao registrada");
        return Dto.ChamadoSaida.de(c);
    }
    @Transactional
    public Dto.ChamadoSaida fechar(Long id, Dto.FechamentoEntrada d) {
        Usuario u = auth.atual();
        Chamado c = bloquear(id);
        dono(u, c);
        precisa(c.status == StatusChamado.RESOLVIDO, "Somente chamado resolvido pode ser fechado.");
        c.nota = d.nota();
        c.fechadoEm = agora();
        status(c, u, StatusChamado.FECHADO, "Avaliacao: " + d.nota());
        return Dto.ChamadoSaida.de(c);
    }
    @Transactional
    public Dto.ChamadoSaida reabrir(Long id) {
        Usuario u = auth.atual();
        Chamado c = bloquear(id);
        dono(u, c);
        precisa(c.status == StatusChamado.RESOLVIDO, "Somente chamado resolvido pode ser reaberto.");
        precisa(RegraDePrazos.podeReabrir(c.resolvidoEm, agora()), "Prazo de 7 dias para reabrir expirou.");
        precisa(c.tecnico != null, "Chamado sem tecnico responsavel.");
        usuarios.bloquear(c.tecnico.id).orElseThrow(() -> RegraException.naoEncontrado("Tecnico"));
        limiteCinco(c.tecnico.id);
        c.resolvidoEm = null;
        c.segundosAteResolucao = null;
        c.solucao = null;
        status(c, u, StatusChamado.EM_ATENDIMENTO, "Reabertura para o mesmo tecnico");
        return Dto.ChamadoSaida.de(c);
    }
    @Transactional
    public Dto.ChamadoSaida mudarPrioridade(Long id, Dto.PrioridadeEntrada d) {
        Usuario u = auth.atual();
        supervisor(u);
        Chamado c = bloquear(id);
        precisa(c.status != StatusChamado.FECHADO, "Chamado fechado nao pode ser alterado.");
        Prioridade antiga = c.prioridade;
        precisa(antiga != d.prioridade(), "Prioridade ja definida.");
        c.prioridade = d.prioridade();
        c.prazoEm = RegraDePrazos.limite(c.abertoEm, horas(d.prioridade()), c.segundosPausados);
        historico(c, u, "prioridade", antiga, d.prioridade(), d.justificativa());
        return Dto.ChamadoSaida.de(c);
    }
    @Transactional
    public Dto.ChamadoSaida reatribuir(Long id, Dto.AtribuicaoEntrada d) {
        Usuario u = auth.atual();
        supervisor(u);
        Chamado c = bloquear(id);
        precisa(c.status == StatusChamado.ABERTO || c.status == StatusChamado.EM_ATENDIMENTO
                || c.status == StatusChamado.AGUARDANDO_SOLICITANTE, "Chamado nao pode ser reatribuido.");
        Usuario novo = usuarios.bloquear(d.tecnicoId()).orElseThrow(() -> RegraException.naoEncontrado("Tecnico"));
        precisa(novo.perfil == Perfil.TECNICO && novo.equipe != null
                && novo.equipe.id.equals(c.equipe.id), "Tecnico deve pertencer a equipe do chamado.");
        precisa(c.tecnico == null || !novo.id.equals(c.tecnico.id), "Tecnico ja responsavel.");
        limiteCinco(novo.id);
        liberarPausa(c, agora());
        Long anterior = c.tecnico == null ? null : c.tecnico.id;
        c.tecnico = novo;
        historico(c, u, "tecnico", anterior, novo.id, d.justificativa());
        if (c.status != StatusChamado.EM_ATENDIMENTO) {
            status(c, u, StatusChamado.EM_ATENDIMENTO, "Reatribuicao: " + d.justificativa());
        }
        return Dto.ChamadoSaida.de(c);
    }
    @Transactional
    public Dto.ProcessamentoSaida processar() {
        Usuario u = auth.atual();
        supervisor(u);
        int abandonados = 0, resolvidos = 0;
        Instant instante = agora();
        for (Chamado item : chamados.findAll()) {
            if (item.status != StatusChamado.AGUARDANDO_SOLICITANTE
                    && item.status != StatusChamado.RESOLVIDO) continue;
            Chamado c = bloquear(item.id);
            if (c.status == StatusChamado.AGUARDANDO_SOLICITANTE && c.pausaIniciadaEm != null
                    && RegraDePrazos.abandonado(c.pausaIniciadaEm, instante)) {
                liberarPausa(c, instante);
                c.fechadoEm = instante;
                status(c, u, StatusChamado.FECHADO, "Abandono apos mais de 5 dias");
                abandonados++;
            } else if (c.status == StatusChamado.RESOLVIDO && c.resolvidoEm != null
                    && RegraDePrazos.resolvidoHaSeteDias(c.resolvidoEm, instante)) {
                c.fechadoEm = instante;
                status(c, u, StatusChamado.FECHADO, "Fechamento automatico apos 7 dias");
                resolvidos++;
            }
        }
        return new Dto.ProcessamentoSaida(abandonados, resolvidos);
    }
}
