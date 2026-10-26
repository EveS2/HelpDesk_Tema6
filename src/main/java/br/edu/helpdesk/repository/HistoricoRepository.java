package br.edu.helpdesk.repository;

import br.edu.helpdesk.model.HistoricoChamado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HistoricoRepository extends JpaRepository<HistoricoChamado, Long> {
    List<HistoricoChamado> findByChamadoIdOrderByDataAlteracaoAsc(Long id);
}
