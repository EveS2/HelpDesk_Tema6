package br.edu.helpdesk.service;

import java.time.Duration;
import java.time.Instant;

/** Calculos puros: sem banco de dados e sem Spring, portanto faceis de testar. */
public final class RegraDePrazos {
    private RegraDePrazos() {}
    public static Instant limite(Instant abertoEm, int horas, long segundosPausados) {
        return abertoEm.plus(Duration.ofHours(horas)).plusSeconds(segundosPausados);
    }
    public static long segundosUteis(Instant abertoEm, Instant resolvidoEm, long segundosPausados) {
        return Math.max(0, Duration.between(abertoEm, resolvidoEm).getSeconds() - segundosPausados);
    }
    public static boolean podeReabrir(Instant resolvidoEm, Instant agora) {
        return !agora.isAfter(resolvidoEm.plus(Duration.ofDays(7)));
    }
    public static boolean abandonado(Instant inicioPausa, Instant agora) {
        return agora.isAfter(inicioPausa.plus(Duration.ofDays(5)));
    }
    public static boolean resolvidoHaSeteDias(Instant resolvidoEm, Instant agora) {
        return !agora.isBefore(resolvidoEm.plus(Duration.ofDays(7)));
    }
}
