package br.edu.helpdesk.service;

import br.edu.helpdesk.dto.Dto;
import br.edu.helpdesk.exception.RegraException;
import br.edu.helpdesk.model.*;
import br.edu.helpdesk.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class ConfiguracaoService {
    private final EquipeRepository equipes;
    private final CategoriaRepository categorias;
    private final UsuarioRepository usuarios;
    private final PrazoRepository prazos;
    private final PasswordEncoder encoder;
    public ConfiguracaoService(EquipeRepository equipes, CategoriaRepository categorias,
                               UsuarioRepository usuarios, PrazoRepository prazos, PasswordEncoder encoder) {
        this.equipes = equipes; this.categorias = categorias;
        this.usuarios = usuarios; this.prazos = prazos; this.encoder = encoder;
    }
    @Transactional
    public Dto.EquipeSaida criarEquipe(Dto.EquipeEntrada d) {
        if (equipes.existsByNomeIgnoreCase(d.nome())) throw RegraException.conflito("Equipe ja existe.");
        return Dto.EquipeSaida.de(equipes.save(new Equipe(d.nome(), d.descricao())));
    }
    @Transactional
    public Dto.CategoriaSaida criarCategoria(Dto.CategoriaEntrada d) {
        if (categorias.existsByNomeIgnoreCase(d.nome())) throw RegraException.conflito("Categoria ja existe.");
        Equipe equipe = equipes.findById(d.equipeId()).orElseThrow(() -> RegraException.naoEncontrado("Equipe"));
        return Dto.CategoriaSaida.de(categorias.save(new Categoria(d.nome(), d.descricao(), equipe)));
    }
    @Transactional
    public Dto.UsuarioSaida criarTecnico(Dto.TecnicoEntrada d) {
        if (usuarios.existsByEmailIgnoreCase(d.email())) throw RegraException.conflito("E-mail ja cadastrado.");
        Equipe equipe = equipes.findById(d.equipeId()).orElseThrow(() -> RegraException.naoEncontrado("Equipe"));
        Usuario u = usuarios.save(new Usuario(d.nome(), d.email().trim().toLowerCase(Locale.ROOT),
                d.setor(), encoder.encode(d.senha()), Perfil.TECNICO, equipe));
        return Dto.UsuarioSaida.de(u);
    }
    @Transactional
    public Dto.PrazoSaida alterarPrazo(Prioridade p, Dto.PrazoEntrada d) {
        PrazoPrioridade prazo = prazos.findById(p).orElseThrow(() -> RegraException.naoEncontrado("Prazo"));
        prazo.horas = d.horas();
        return new Dto.PrazoSaida(p, prazo.horas);
    }
    @Transactional(readOnly = true)
    public List<Dto.EquipeSaida> listarEquipes() {
        return equipes.findAll().stream().map(Dto.EquipeSaida::de).toList();
    }
    @Transactional(readOnly = true)
    public List<Dto.CategoriaSaida> listarCategorias() {
        return categorias.findAll().stream().map(Dto.CategoriaSaida::de).toList();
    }
    @Transactional(readOnly = true)
    public List<Dto.PrazoSaida> listarPrazos() {
        return prazos.findAll().stream().map(p -> new Dto.PrazoSaida(p.prioridade, p.horas)).toList();
    }
}
