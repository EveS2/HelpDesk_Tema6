package br.edu.helpdesk.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class Chamado {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(nullable = false)
    public String titulo;
    @Column(nullable = false, length = 4000)
    public String descricao;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    public Prioridade prioridade;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    public StatusChamado status;
    @ManyToOne(optional = false)
    public Categoria categoria;
    @ManyToOne(optional = false)
    public Equipe equipe;
    @ManyToOne(optional = false)
    public Usuario solicitante;
    @ManyToOne
    public Usuario tecnico;
    @Column(nullable = false)
    public Instant abertoEm;
    @Column(nullable = false)
    public Instant prazoEm;
    public Instant pausaIniciadaEm;
    @Column(nullable = false)
    public long segundosPausados;
    public Instant resolvidoEm;
    public Instant fechadoEm;
    @Column(length = 4000)
    public String solucao;
    public Integer nota;
    public Long segundosAteResolucao;
    @Version
    public long versao;
    protected Chamado() {}
    public Chamado(String titulo, String descricao, Prioridade prioridade,
                   Categoria categoria, Usuario solicitante, Instant agora, Instant prazo) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.prioridade = prioridade;
        this.status = StatusChamado.ABERTO;
        this.categoria = categoria;
        this.equipe = categoria.equipe;
        this.solicitante = solicitante;
        this.abertoEm = agora;
        this.prazoEm = prazo;
        this.segundosPausados = 0;
    }
}
