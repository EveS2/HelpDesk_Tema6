package br.edu.helpdesk.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class HistoricoChamado {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @ManyToOne(optional = false)
    public Chamado chamado;
    @ManyToOne(optional = false)
    public Usuario autor;
    @Column(nullable = false)
    public Instant dataAlteracao;
    @Column(nullable = false)
    public String campoAlterado;
    public String valorAntigo;
    public String valorNovo;
    public String justificativa;
    protected HistoricoChamado() {}
    public HistoricoChamado(Chamado chamado, Usuario autor, Instant agora,
                           String campo, String antigo, String novo, String justificativa) {
        this.chamado = chamado;
        this.autor = autor;
        this.dataAlteracao = agora;
        this.campoAlterado = campo;
        this.valorAntigo = antigo;
        this.valorNovo = novo;
        this.justificativa = justificativa;
    }
}
