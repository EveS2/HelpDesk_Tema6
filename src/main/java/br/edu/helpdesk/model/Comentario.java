package br.edu.helpdesk.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class Comentario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @ManyToOne(optional = false)
    public Chamado chamado;
    @ManyToOne(optional = false)
    public Usuario autor;
    @Column(nullable = false, length = 4000)
    public String texto;
    @Column(nullable = false)
    public boolean interno;
    @Column(nullable = false)
    public Instant criadoEm;
    protected Comentario() {}
    public Comentario(Chamado chamado, Usuario autor, String texto, boolean interno, Instant criadoEm) {
        this.chamado = chamado;
        this.autor = autor;
        this.texto = texto;
        this.interno = interno;
        this.criadoEm = criadoEm;
    }
}
