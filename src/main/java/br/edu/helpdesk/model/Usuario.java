package br.edu.helpdesk.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(nullable = false)
    public String nome;
    @Column(nullable = false, unique = true)
    public String email;
    public String setor;
    @Column(nullable = false)
    public String senha;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    public Perfil perfil;
    @ManyToOne
    public Equipe equipe;
    protected Usuario() {}
    public Usuario(String nome, String email, String setor, String senha, Perfil perfil, Equipe equipe) {
        this.nome = nome;
        this.email = email;
        this.setor = setor;
        this.senha = senha;
        this.perfil = perfil;
        this.equipe = equipe;
    }
}
