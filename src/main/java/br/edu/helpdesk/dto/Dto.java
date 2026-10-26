package br.edu.helpdesk.dto;

import br.edu.helpdesk.model.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;

/** Entrada e saida da API: nunca devolvemos entidades JPA diretamente. */
public final class Dto {
    private Dto() {}
    public record Cadastro(@NotBlank String nome, @NotBlank @Email String email,
                           @NotBlank String setor, @NotBlank @Size(min = 8) String senha) {}
    public record Login(String email, String senha) {}
    public record Token(String token, String tipo, String perfil) {}
    public record UsuarioSaida(Long id, String nome, String email, String setor, Perfil perfil, Long equipeId) {
        public static UsuarioSaida de(Usuario u) {
            return new UsuarioSaida(u.id, u.nome, u.email, u.setor, u.perfil,
                    u.equipe == null ? null : u.equipe.id);
        }
    }
    public record EquipeEntrada(@NotBlank String nome, String descricao) {}
    public record EquipeSaida(Long id, String nome, String descricao) {
        public static EquipeSaida de(Equipe e) { return new EquipeSaida(e.id, e.nome, e.descricao); }
    }
    public record TecnicoEntrada(@NotBlank String nome, @NotBlank @Email String email,
                                @NotBlank String setor, @NotBlank @Size(min = 8) String senha,
                                @NotNull Long equipeId) {}
    public record CategoriaEntrada(@NotBlank String nome, String descricao, @NotNull Long equipeId) {}
    public record CategoriaSaida(Long id, String nome, String descricao, Long equipeId) {
        public static CategoriaSaida de(Categoria c) {
            return new CategoriaSaida(c.id, c.nome, c.descricao, c.equipe.id);
        }
    }
    public record PrazoEntrada(@Min(1) int horas) {}
    public record PrazoSaida(Prioridade prioridade, int horas) {}
    public record ChamadoEntrada(@NotBlank String titulo, @NotBlank String descricao,
                                @NotNull Long categoriaId, @NotNull Prioridade prioridade) {}
    public record ChamadoSaida(Long id, String titulo, String descricao, Prioridade prioridade,
                              StatusChamado status, Long categoriaId, Long equipeId,
                              Long solicitanteId, Long tecnicoId, Instant abertoEm,
                              Instant prazoEm, Instant pausaIniciadaEm, Instant resolvidoEm,
                              Instant fechadoEm, String solucao, Integer nota) {
        public static ChamadoSaida de(Chamado c) {
            return new ChamadoSaida(c.id, c.titulo, c.descricao, c.prioridade, c.status,
                    c.categoria.id, c.equipe.id, c.solicitante.id,
                    c.tecnico == null ? null : c.tecnico.id, c.abertoEm, c.prazoEm,
                    c.pausaIniciadaEm, c.resolvidoEm, c.fechadoEm, c.solucao, c.nota);
        }
    }
    public record TextoEntrada(@NotBlank String texto) {}
    public record ComentarioEntrada(@NotBlank String texto, boolean interno) {}
    public record ComentarioSaida(Long id, Long autorId, String texto, boolean interno, Instant criadoEm) {
        public static ComentarioSaida de(Comentario c) {
            return new ComentarioSaida(c.id, c.autor.id, c.texto, c.interno, c.criadoEm);
        }
    }
    public record TransferenciaEntrada(@NotNull Long equipeId, @NotBlank String justificativa) {}
    public record FechamentoEntrada(@NotNull @Min(1) @Max(5) Integer nota) {}
    public record PrioridadeEntrada(@NotNull Prioridade prioridade, @NotBlank String justificativa) {}
    public record AtribuicaoEntrada(@NotNull Long tecnicoId, @NotBlank String justificativa) {}
    public record HistoricoSaida(Instant quando, String campo, String de, String para, String justificativa, Long autorId) {
        public static HistoricoSaida de(HistoricoChamado h) {
            return new HistoricoSaida(h.dataAlteracao, h.campoAlterado, h.valorAntigo,
                    h.valorNovo, h.justificativa, h.autor.id);
        }
    }
    public record ProcessamentoSaida(int abandonadosFechados, int resolvidosFechados) {}
    public record AlertaPrazo(Long chamadoId, Instant prazo, String tipo, Long equipeId) {}
    public record MediaTempo(String grupo, long quantidade, double horasMedias) {}
    public record MediaNota(Long tecnicoId, String tecnicoNome, long quantidade, double media) {}
    public record RelatorioMedias(List<MediaTempo> porEquipe, List<MediaTempo> porCategoria) {}
}
