package br.edu.helpdesk.controller;

import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import java.time.temporal.ChronoUnit;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ApiFluxoTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @MockitoBean Clock relogio;
    final Instant inicio = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    @BeforeEach void agoraInicial() { when(relogio.instant()).thenReturn(inicio); }
    String executar(String metodo, String url, String jwt, String corpo, int esperado) throws Exception {
        var chamada = switch (metodo) {
            case "GET" -> get(url);
            case "POST" -> post(url);
            case "PUT" -> put(url);
            case "PATCH" -> patch(url);
            default -> throw new IllegalArgumentException(metodo);
        };
        if (jwt != null) chamada.header("Authorization", "Bearer " + jwt);
        if (corpo != null) chamada.contentType(MediaType.APPLICATION_JSON).content(corpo);
        return mvc.perform(chamada).andExpect(status().is(esperado))
                .andReturn().getResponse().getContentAsString();
    }
    String login(String email, String senha) throws Exception {
        String corpo = json.writeValueAsString(java.util.Map.of("email", email, "senha", senha));
        return json.readTree(executar("POST", "/api/auth/login", null, corpo, 200)).get("token").asText();
    }
    String novoSolicitante(String email) throws Exception {
        String body = json.writeValueAsString(java.util.Map.of(
                "nome", "Solicitante Teste", "email", email, "setor", "Escola", "senha", "Senha123!"));
        String resposta = executar("POST", "/api/auth/cadastro", null, body, 201);
        assertFalse(resposta.contains("Senha123!"));
        assertFalse(resposta.contains("\"senha\""));
        return login(email, "Senha123!");
    }
    long abrir(String solicitante, String titulo) throws Exception {
        return abrir(solicitante, titulo, 8);
    }
    long abrir(String solicitante, String titulo, long horasEsperadas) throws Exception {
        var categorias = json.readTree(executar("GET", "/api/config/categorias", solicitante, null, 200));
        long categoriaId = categorias.get(0).get("id").asLong();
        String corpo = json.writeValueAsString(java.util.Map.of(
                "titulo", titulo, "descricao", "Erro no computador", "prioridade", "ALTA", "categoriaId", categoriaId));
        JsonNode saida = json.readTree(executar("POST", "/api/chamados", solicitante, corpo, 201));
        assertEquals("ABERTO", saida.get("status").asText());
        assertEquals(inicio.plus(Duration.ofHours(horasEsperadas)).toString(), saida.get("prazoEm").asText());
        return saida.get("id").asLong();
    }
    @Test void jornadaCompletaPrivacidadeRelatoriosEHistorico() throws Exception {
        String solicitante = novoSolicitante("jornada@exemplo.com");
        String outro = novoSolicitante("outro@exemplo.com");
        String tecnico = login("tecnico@escola.com", "Senha123!");
        String supervisor = login("supervisor@escola.com", "Senha123!");
        long id = abrir(solicitante, "Impressora com defeito");
        executar("GET", "/api/chamados/" + id, outro, null, 403);
        executar("POST", "/api/chamados/" + id + "/assumir", solicitante, null, 403);
        executar("POST", "/api/chamados/" + id + "/assumir", tecnico, null, 200);
        executar("POST", "/api/chamados/" + id + "/comentarios", tecnico,
                "{\"texto\":\"Senha da maquina\",\"interno\":true}", 201);
        executar("POST", "/api/chamados/" + id + "/comentarios", tecnico,
                "{\"texto\":\"Pode testar?\",\"interno\":false}", 201);
        String visivel = executar("GET", "/api/chamados/" + id + "/comentarios", solicitante, null, 200);
        assertFalse(visivel.contains("Senha da maquina"));
        assertTrue(visivel.contains("Pode testar?"));
        executar("POST", "/api/chamados/" + id + "/comentarios", solicitante,
                "{\"texto\":\"Nota escondida\",\"interno\":true}", 403);
        when(relogio.instant()).thenReturn(inicio.plus(Duration.ofMinutes(30)));
        executar("POST", "/api/chamados/" + id + "/aguardar", tecnico, null, 200);
        when(relogio.instant()).thenReturn(inicio.plus(Duration.ofHours(2)));
        JsonNode respondido = json.readTree(executar("POST", "/api/chamados/" + id + "/comentarios",
                solicitante, "{\"texto\":\"Voltei!\"}", 201));
        assertEquals("Voltei!", respondido.get("texto").asText());
        JsonNode atual = json.readTree(executar("GET", "/api/chamados/" + id, solicitante, null, 200));
        assertEquals("EM_ATENDIMENTO", atual.get("status").asText());
        assertEquals(inicio.plus(Duration.ofHours(9)).plus(Duration.ofMinutes(30)).toString(), atual.get("prazoEm").asText());
        when(relogio.instant()).thenReturn(inicio.plus(Duration.ofHours(3)));
        executar("POST", "/api/chamados/" + id + "/resolver", tecnico, "{\"texto\":\"Resolvi o defeito\"}", 200);
        executar("POST", "/api/chamados/" + id + "/fechar", solicitante, "{\"nota\":5}", 200);
        String notas = executar("GET", "/api/relatorios/notas", supervisor, null, 200);
        assertTrue(notas.contains("5.0"));
        String tempos = executar("GET", "/api/relatorios/tempos", supervisor, null, 200);
        assertTrue(tempos.contains("1.5"));
        String historico = executar("GET", "/api/chamados/" + id + "/historico", solicitante, null, 200);
        assertTrue(historico.contains("status"));
        executar("GET", "/api/relatorios/prazos", solicitante, null, 403);
    }
    @Test void alteracaoTransferenciaProcessamentoPrazosEValidacao() throws Exception {
        String solicitante = novoSolicitante("controle@exemplo.com");
        String tecnico = login("tecnico@escola.com", "Senha123!");
        String supervisor = login("supervisor@escola.com", "Senha123!");
        executar("POST", "/api/auth/login", null, "{\"email\":\"nao@exemplo.com\",\"senha\":\"errado\"}", 401);
        executar("POST", "/api/auth/cadastro", null,
                "{\"nome\":\"X\",\"email\":\"x@exemplo.com\",\"setor\":\"TI\",\"senha\":\"curta\"}", 400);
        long id = abrir(solicitante, "Rede sem conexao");
        executar("PATCH", "/api/chamados/" + id + "/prioridade", supervisor,
                "{\"prioridade\":\"CRITICA\",\"justificativa\":\"Urgente\"}", 200);
        JsonNode ticket = json.readTree(executar("GET", "/api/chamados/" + id, supervisor, null, 200));
        assertEquals(inicio.plus(Duration.ofHours(4)).toString(), ticket.get("prazoEm").asText());
        String eq = executar("POST", "/api/config/equipes", supervisor,
                "{\"nome\":\"Redes\",\"descricao\":\"Equipe de rede\"}", 201);
        long equipe = json.readTree(eq).get("id").asLong();
        executar("POST", "/api/config/categorias", supervisor,
                "{\"nome\":\"Wifi\",\"equipeId\":" + equipe + "}", 201);
        executar("POST", "/api/config/tecnicos", supervisor,
                "{\"nome\":\"Tecnica redes\",\"email\":\"redes@escola.com\",\"setor\":\"TI\",\"senha\":\"Senha123!\",\"equipeId\":" + equipe + "}", 201);
        executar("POST", "/api/chamados/" + id + "/assumir", tecnico, null, 200);
        executar("POST", "/api/chamados/" + id + "/transferir", tecnico,
                "{\"equipeId\":" + equipe + ",\"justificativa\":\"Problema de rede\"}", 200);
        executar("POST", "/api/chamados/" + id + "/assumir", tecnico, null, 403);
        String tecnicoRedes = login("redes@escola.com", "Senha123!");
        executar("POST", "/api/chamados/" + id + "/assumir", tecnicoRedes, null, 200);
        executar("POST", "/api/chamados/" + id + "/aguardar", tecnicoRedes, null, 200);
        when(relogio.instant()).thenReturn(inicio.plus(Duration.ofDays(6)));
        String novoSupervisor = login("supervisor@escola.com", "Senha123!");
        JsonNode processados = json.readTree(executar("POST", "/api/chamados/processar-pendentes", novoSupervisor, null, 200));
        assertEquals(1, processados.get("abandonadosFechados").asInt());
        assertEquals("FECHADO", json.readTree(executar("GET", "/api/chamados/" + id,
                novoSupervisor, null, 200)).get("status").asText());
    }
    @Test void cincoChamadosLimiteEReaberturaMantemTecnico() throws Exception {
        String solicitante = novoSolicitante("cinco@exemplo.com");
        String tecnico = login("tecnico@escola.com", "Senha123!");
        long primeiro = -1;
        for (int i = 0; i < 5; i++) {
            long id = abrir(solicitante, "Falha teste " + i);
            if (i == 0) primeiro = id;
            executar("POST", "/api/chamados/" + id + "/assumir", tecnico, null, 200);
        }
        long sexto = abrir(solicitante, "Sexto chamado");
        executar("POST", "/api/chamados/" + sexto + "/assumir", tecnico, null, 409);
        executar("POST", "/api/chamados/" + primeiro + "/resolver", tecnico,
                "{\"texto\":\"Resolvido\"}", 200);
        executar("POST", "/api/chamados/" + sexto + "/assumir", tecnico, null, 200);
        executar("POST", "/api/chamados/" + primeiro + "/reabrir", solicitante, null, 409);
        executar("POST", "/api/chamados/" + sexto + "/resolver", tecnico,
                "{\"texto\":\"Resolvido\"}", 200);
        String reaberto = executar("POST", "/api/chamados/" + primeiro + "/reabrir", solicitante, null, 200);
        assertEquals("EM_ATENDIMENTO", json.readTree(reaberto).get("status").asText());
        assertFalse(json.readTree(reaberto).get("tecnicoId").isNull());
    }
    @Test void relatorioAlertaFechamentoSemNotaEPrazoConfiguravel() throws Exception {
        String solicitante = novoSolicitante("relatorio@exemplo.com");
        String tecnico = login("tecnico@escola.com", "Senha123!");
        String supervisor = login("supervisor@escola.com", "Senha123!");
        executar("PUT", "/api/config/prazos/ALTA", supervisor, "{\"horas\":2}", 200);
        long id = abrir(solicitante, "Urgencia relatorio", 2);
        String alertas = executar("GET", "/api/relatorios/prazos", supervisor, null, 200);
        assertTrue(alertas.contains("VENCE_EM_ATE_2H"));
        executar("POST", "/api/chamados/" + id + "/assumir", tecnico, null, 200);
        executar("POST", "/api/chamados/" + id + "/resolver", tecnico, "{\"texto\":\"Pronto\"}", 200);
        when(relogio.instant()).thenReturn(inicio.plus(Duration.ofDays(7)));
        String supervisorNovo = login("supervisor@escola.com", "Senha123!");
        String saida = executar("POST", "/api/chamados/processar-pendentes", supervisorNovo, null, 200);
        assertTrue(saida.contains("resolvidosFechados\":1"));
        JsonNode chamado = json.readTree(executar("GET", "/api/chamados/" + id, supervisorNovo, null, 200));
        assertTrue(chamado.get("nota").isNull());
        assertEquals("FECHADO", chamado.get("status").asText());
    }


    // RF06 e RF08: o técnico já tem cinco chamados em atendimento,
    // então a resposta do solicitante não pode gerar um sexto atendimento.
    @Test void respostaComCincoAtivosDevolveChamadoParaFila() throws Exception {
        String solicitante = novoSolicitante("retorno-limite@exemplo.com");
        String tecnico = login("tecnico@escola.com", "Senha123!");

        // Primeiro chamado: sai da contagem de ativos enquanto aguarda resposta.
        long aguardando = abrir(solicitante, "Equipamento aguardando resposta");
        executar("POST", "/api/chamados/" + aguardando + "/assumir", tecnico, null, 200);
        executar("POST", "/api/chamados/" + aguardando + "/aguardar", tecnico, null, 200);

        // O mesmo técnico assume exatamente outros cinco chamados.
        for (int i = 0; i < 5; i++) {
            long ativo = abrir(solicitante, "Atendimento simultaneo " + i);
            executar("POST", "/api/chamados/" + ativo + "/assumir", tecnico, null, 200);
        }

        // Quando o solicitante responde, o antigo chamado deve voltar à fila.
        when(relogio.instant()).thenReturn(inicio.plus(Duration.ofMinutes(40)));
        executar("POST", "/api/chamados/" + aguardando + "/comentarios", solicitante,
                "{\"texto\":\"Ja consegui testar\",\"interno\":false}", 201);
        JsonNode chamado = json.readTree(executar("GET", "/api/chamados/" + aguardando,
                solicitante, null, 200));
        assertEquals("ABERTO", chamado.get("status").asText());
        assertTrue(chamado.get("tecnicoId").isNull(), "Chamado deve ficar sem tecnico na fila");
        assertTrue(chamado.get("pausaIniciadaEm").isNull(), "O prazo precisa voltar a correr");
        assertEquals(inicio.plus(Duration.ofHours(8)).plus(Duration.ofMinutes(40)).toString(),
                chamado.get("prazoEm").asText());

        // RF09: mesmo essa alteração automática precisa constar no histórico.
        JsonNode historico = json.readTree(executar("GET", "/api/chamados/" + aguardando
                + "/historico", solicitante, null, 200));
        boolean registrouFila = false;
        boolean registrouTecnico = false;
        for (JsonNode evento : historico) {
            if ("status".equals(evento.path("campo").asText())
                    && "ABERTO".equals(evento.path("para").asText())
                    && "Resposta recebida; voltou a fila".equals(
                            evento.path("justificativa").asText())) {
                registrouFila = true;
            }
            if ("tecnico".equals(evento.path("campo").asText())
                    && "null".equals(evento.path("para").asText())) {
                registrouTecnico = true;
            }
        }
        assertTrue(registrouFila, "Deve registrar a volta para a fila no historico");
        assertTrue(registrouTecnico, "Deve registrar a retirada do tecnico no historico");
    }

    // RF07: o mesmo conjunto de comentários deve aparecer de forma diferente
    // para o solicitante e para quem atua no suporte.
    @Test void notasInternasFicamOcultasParaSolicitante() throws Exception {
        String solicitante = novoSolicitante("notas-privadas@exemplo.com");
        String tecnico = login("tecnico@escola.com", "Senha123!");
        String supervisor = login("supervisor@escola.com", "Senha123!");
        long id = abrir(solicitante, "Problema de acesso");
        executar("POST", "/api/chamados/" + id + "/assumir", tecnico, null, 200);
        executar("POST", "/api/chamados/" + id + "/comentarios", tecnico,
                "{\"texto\":\"Nota exclusiva da equipe\",\"interno\":true}", 201);
        executar("POST", "/api/chamados/" + id + "/comentarios", tecnico,
                "{\"texto\":\"Mensagem visivel\",\"interno\":false}", 201);
        executar("POST", "/api/chamados/" + id + "/comentarios", solicitante,
                "{\"texto\":\"Obrigado\",\"interno\":false}", 201);

        JsonNode vistoPeloSolicitante = json.readTree(executar("GET", "/api/chamados/" + id
                + "/comentarios", solicitante, null, 200));
        assertEquals(2, vistoPeloSolicitante.size());
        for (JsonNode comentario : vistoPeloSolicitante) {
            assertFalse(comentario.path("interno").asBoolean());
            assertNotEquals("Nota exclusiva da equipe", comentario.path("texto").asText());
        }

        JsonNode vistoPeloTecnico = json.readTree(executar("GET", "/api/chamados/" + id
                + "/comentarios", tecnico, null, 200));
        assertEquals(3, vistoPeloTecnico.size());
        assertTrue(vistoPeloTecnico.toString().contains("Nota exclusiva da equipe"));

        JsonNode vistoPeloSupervisor = json.readTree(executar("GET", "/api/chamados/" + id
                + "/comentarios", supervisor, null, 200));
        assertEquals(3, vistoPeloSupervisor.size());
    }

}
