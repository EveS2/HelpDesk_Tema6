package br.edu.helpdesk.model;

import jakarta.persistence.*;

@Entity
public class Categoria {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(nullable = false, unique = true)
    public String nome;
    public String descricao;
    @ManyToOne(optional = false)
    public Equipe equipe;
    protected Categoria() {}
    public Categoria(String nome, String descricao, Equipe equipe) {
        this.nome = nome;
        this.descricao = descricao;
        this.equipe = equipe;
    }
}
