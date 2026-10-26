package br.edu.helpdesk.service;

import br.edu.helpdesk.dto.Dto;
import br.edu.helpdesk.exception.RegraException;
import br.edu.helpdesk.model.*;
import br.edu.helpdesk.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.Locale;

@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder senhas;
    private final JwtEncoder tokens;
    private final Clock relogio;
    private final int horasJwt;
    public AuthService(UsuarioRepository usuarios, PasswordEncoder senhas, JwtEncoder tokens,
                       Clock relogio, @Value("${app.jwt.hours:8}") int horasJwt) {
        this.usuarios = usuarios;
        this.senhas = senhas;
        this.tokens = tokens;
        this.relogio = relogio;
        this.horasJwt = horasJwt;
    }
    @Transactional
    public Dto.UsuarioSaida cadastrar(Dto.Cadastro d) {
        String email = d.email().trim().toLowerCase(Locale.ROOT);
        if (usuarios.existsByEmailIgnoreCase(email)) throw RegraException.conflito("E-mail ja cadastrado.");
        Usuario u = usuarios.save(new Usuario(d.nome(), email, d.setor(),
                senhas.encode(d.senha()), Perfil.SOLICITANTE, null));
        return Dto.UsuarioSaida.de(u);
    }
    @Transactional(readOnly = true)
    public Dto.Token entrar(Dto.Login d) {
        if (d.email() == null || d.senha() == null || d.email().isBlank() || d.senha().isBlank()) {
            throw new RegraException(HttpStatus.UNAUTHORIZED, "E-mail ou senha invalidos.");
        }
        Usuario u = usuarios.findByEmailIgnoreCase(d.email().trim()).orElse(null);
        if (u == null || !senhas.matches(d.senha(), u.senha)) {
            throw new RegraException(HttpStatus.UNAUTHORIZED, "E-mail ou senha invalidos.");
        }
        Instant agora = relogio.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("helpdesk-escolas")
                .issuedAt(agora).expiresAt(agora.plus(Duration.ofHours(horasJwt)))
                .subject(u.id.toString()).claim("perfil", u.perfil.name()).build();
        String token = tokens.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new Dto.Token(token, "Bearer", u.perfil.name());
    }
    @Transactional(readOnly = true)
    public Usuario atual() {
        String id = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarios.findById(Long.parseLong(id))
                .orElseThrow(() -> RegraException.naoEncontrado("Usuario"));
    }
}
