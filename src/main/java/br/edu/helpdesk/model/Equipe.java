package br.edu.helpdesk.model;

import jakarta.persistence.*;

@Entity
public class Equipe {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(nullable = false, unique = true)
    public String nome;
    public String descricao;
    protected Equipe() {}
    public Equipe(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }
}
