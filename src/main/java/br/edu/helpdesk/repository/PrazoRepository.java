package br.edu.helpdesk.repository;

import br.edu.helpdesk.model.PrazoPrioridade;
import br.edu.helpdesk.model.Prioridade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrazoRepository extends JpaRepository<PrazoPrioridade, Prioridade> {}
