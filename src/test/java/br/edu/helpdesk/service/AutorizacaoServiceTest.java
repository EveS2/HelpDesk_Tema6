package br.edu.helpdesk.service;

import br.edu.helpdesk.dto.Dto;
import br.edu.helpdesk.exception.RegraException;
import br.edu.helpdesk.model.*;
import br.edu.helpdesk.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutorizacaoServiceTest {
    @Mock ChamadoRepository chamados;
    @Mock UsuarioRepository usuarios;
    @Mock CategoriaRepository categorias;
    @Mock EquipeRepository equipes;
    @Mock PrazoRepository prazos;
    @Mock ComentarioRepository comentarios;
    @Mock HistoricoRepository historicos;
    @Mock AuthService auth;
    @Mock Clock clock;
    @InjectMocks ChamadoService service;
    @Test void solicitanteNaoPodeVerChamadoDeOutraPessoa() {
        Usuario um = new Usuario("Um", "um@x.com", "TI", "hash", Perfil.SOLICITANTE, null);
        Usuario dois = new Usuario("Dois", "dois@x.com", "TI", "hash", Perfil.SOLICITANTE, null);
        um.id = 1L; dois.id = 2L;
        Equipe equipe = new Equipe("TI", "Suporte"); equipe.id = 3L;
        Categoria cat = new Categoria("Micro", "", equipe); cat.id = 4L;
        Chamado c = new Chamado("Defeito", "Sem video", Prioridade.ALTA,
                cat, um, Instant.now(), Instant.now().plusSeconds(3600));
        c.id = 5L;
        when(auth.atual()).thenReturn(dois);
        when(chamados.findById(5L)).thenReturn(Optional.of(c));
        assertThrows(RegraException.class, () -> service.detalhar(5L));
    }
    @Test void assumirRecusaTecnicoDeOutraEquipe() {
        Equipe a = new Equipe("A", ""); a.id = 10L;
        Equipe b = new Equipe("B", ""); b.id = 11L;
        Usuario tecnico = new Usuario("Tec", "tec@x.com", "TI", "hash", Perfil.TECNICO, b);
        tecnico.id = 20L;
        Usuario solicitante = new Usuario("Sol", "sol@x.com", "TI", "hash", Perfil.SOLICITANTE, null);
        solicitante.id = 21L;
        Categoria categoria = new Categoria("Acesso", "", a);
        Chamado c = new Chamado("Problema", "Descricao", Prioridade.MEDIA,
                categoria, solicitante, Instant.now(), Instant.now().plusSeconds(3600));
        when(auth.atual()).thenReturn(tecnico);
        when(usuarios.bloquear(20L)).thenReturn(Optional.of(tecnico));
        when(chamados.bloquear(30L)).thenReturn(Optional.of(c));
        assertThrows(RegraException.class, () -> service.assumir(30L));
        verify(chamados, never()).save(any());
    }
}
