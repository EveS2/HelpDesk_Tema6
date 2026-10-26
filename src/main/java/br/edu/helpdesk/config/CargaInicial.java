package br.edu.helpdesk.config;

import br.edu.helpdesk.model.*;
import br.edu.helpdesk.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class CargaInicial {
    @Bean CommandLineRunner preparar(UsuarioRepository usuarios, EquipeRepository equipes,
                                     CategoriaRepository categorias, PrazoRepository prazos,
                                     PasswordEncoder senha) {
        return args -> {
            for (Prioridade p : Prioridade.values()) {
                int horas = switch (p) {
                    case BAIXA -> 72;
                    case MEDIA -> 24;
                    case ALTA -> 8;
                    case CRITICA -> 4;
                };
                if (!prazos.existsById(p)) prazos.save(new PrazoPrioridade(p, horas));
            }
            Equipe ti = equipes.findAll().stream().filter(e -> e.nome.equals("TI"))
                    .findFirst().orElseGet(() -> equipes.save(new Equipe("TI", "Suporte geral")));
            if (!categorias.existsByNomeIgnoreCase("Computadores")) {
                categorias.save(new Categoria("Computadores", "Desktops e notebooks", ti));
            }
            if (!usuarios.existsByEmailIgnoreCase("supervisor@escola.com")) {
                usuarios.save(new Usuario("Supervisora", "supervisor@escola.com", "TI",
                        senha.encode("Senha123!"), Perfil.SUPERVISOR, null));
            }
            if (!usuarios.existsByEmailIgnoreCase("tecnico@escola.com")) {
                usuarios.save(new Usuario("Tecnico inicial", "tecnico@escola.com", "TI",
                        senha.encode("Senha123!"), Perfil.TECNICO, ti));
            }
        };
    }
}
