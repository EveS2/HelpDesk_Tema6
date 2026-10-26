import br.edu.helpdesk.service.RegraDePrazos;
import java.time.*;

public class TestePrazosJdk {
    public static void main(String[] args) {
        Instant inicio = Instant.parse("2026-10-25T10:00:00Z");
        verificar(RegraDePrazos.limite(inicio, 72, 0).equals(inicio.plus(Duration.ofHours(72))));
        verificar(RegraDePrazos.limite(inicio, 24, 7200).equals(inicio.plus(Duration.ofHours(26))));
        verificar(RegraDePrazos.segundosUteis(inicio, inicio.plusSeconds(7200), 1800) == 5400);
        verificar(RegraDePrazos.podeReabrir(inicio, inicio.plus(Duration.ofDays(7))));
        verificar(!RegraDePrazos.podeReabrir(inicio, inicio.plus(Duration.ofDays(7)).plusSeconds(1)));
        verificar(!RegraDePrazos.abandonado(inicio, inicio.plus(Duration.ofDays(5))));
        verificar(RegraDePrazos.abandonado(inicio, inicio.plus(Duration.ofDays(5)).plusSeconds(1)));
        verificar(RegraDePrazos.resolvidoHaSeteDias(inicio, inicio.plus(Duration.ofDays(7))));
        System.out.println("OK: 8 verificacoes reais de regras de prazo com Java 21.");
    }
    private static void verificar(boolean resultado) {
        if (!resultado) throw new AssertionError("Regra de prazo reprovada");
    }
}
