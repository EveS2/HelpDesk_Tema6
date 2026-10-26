package br.edu.helpdesk.model;

import jakarta.persistence.*;

@Entity
public class PrazoPrioridade {
    @Id @Enumerated(EnumType.STRING)
    public Prioridade prioridade;
    @Column(nullable = false)
    public int horas;
    protected PrazoPrioridade() {}
    public PrazoPrioridade(Prioridade prioridade, int horas) {
        this.prioridade = prioridade;
        this.horas = horas;
    }
}
