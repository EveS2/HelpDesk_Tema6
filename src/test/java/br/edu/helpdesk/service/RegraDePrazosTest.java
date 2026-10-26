package br.edu.helpdesk.service;

import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;

class RegraDePrazosTest {
    final Instant inicio = Instant.parse("2026-10-25T10:00:00Z");
    @Test void prazosPadraoESegundosDePausa() {
        assertEquals(inicio.plus(Duration.ofHours(4)), RegraDePrazos.limite(inicio, 4, 0));
        assertEquals(inicio.plus(Duration.ofHours(25)), RegraDePrazos.limite(inicio, 24, 3600));
        assertEquals(14400, RegraDePrazos.segundosUteis(inicio, inicio.plusSeconds(18000), 3600));
    }
    @Test void reaberturaAteSeteDiasIncluiInstanteLimite() {
        assertTrue(RegraDePrazos.podeReabrir(inicio, inicio.plus(Duration.ofDays(7))));
        assertFalse(RegraDePrazos.podeReabrir(inicio, inicio.plus(Duration.ofDays(7)).plusSeconds(1)));
    }
    @Test void abandonoExigeMaisDeCincoDiasEFechamentoAposSete() {
        assertFalse(RegraDePrazos.abandonado(inicio, inicio.plus(Duration.ofDays(5))));
        assertTrue(RegraDePrazos.abandonado(inicio, inicio.plus(Duration.ofDays(5)).plusSeconds(1)));
        assertFalse(RegraDePrazos.resolvidoHaSeteDias(inicio, inicio.plus(Duration.ofDays(6))));
        assertTrue(RegraDePrazos.resolvidoHaSeteDias(inicio, inicio.plus(Duration.ofDays(7))));
    }
}
