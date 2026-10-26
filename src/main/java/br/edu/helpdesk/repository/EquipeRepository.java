package br.edu.helpdesk.repository;

import br.edu.helpdesk.model.Equipe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipeRepository extends JpaRepository<Equipe, Long> {
    boolean existsByNomeIgnoreCase(String nome);
}
