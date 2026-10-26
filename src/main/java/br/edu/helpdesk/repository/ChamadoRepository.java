package br.edu.helpdesk.repository;

import br.edu.helpdesk.model.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Chamado c where c.id = :id")
    Optional<Chamado> bloquear(@Param("id") Long id);
    List<Chamado> findBySolicitanteIdOrderByAbertoEmDesc(Long id);
    List<Chamado> findByEquipeIdOrderByAbertoEmDesc(Long id);
    long countByTecnicoIdAndStatus(Long id, StatusChamado status);
}
