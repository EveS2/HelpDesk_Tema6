package br.edu.helpdesk.repository;

import br.edu.helpdesk.model.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    List<Comentario> findByChamadoIdOrderByCriadoEmAsc(Long id);
}
