import com.sun.source.util.JavacTask;
import javax.tools.*;
import java.nio.file.*;
import java.util.*;

/** Confere somente a gramática Java, sem precisar baixar Spring ou Maven. */
public class VerificarSintaxeJdk {
    public static void main(String[] args) throws Exception {
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        List<java.io.File> fontes = new ArrayList<>();
        try (var arquivos = Files.walk(Path.of("src"))) {
            arquivos.filter(p -> p.toString().endsWith(".java"))
                    .forEach(p -> fontes.add(p.toFile()));
        }
        DiagnosticCollector<JavaFileObject> erros = new DiagnosticCollector<>();
        try (StandardJavaFileManager gerenciador = javac.getStandardFileManager(erros, null, null)) {
            var unidades = gerenciador.getJavaFileObjectsFromFiles(fontes);
            JavacTask tarefa = (JavacTask) javac.getTask(null, gerenciador, erros,
                    List.of("-proc:none"), null, unidades);
            tarefa.parse();
        }
        var falhas = erros.getDiagnostics().stream()
                .filter(d -> d.getKind() == Diagnostic.Kind.ERROR).toList();
        for (var erro : falhas) System.err.println(erro);
        if (!falhas.isEmpty()) throw new AssertionError("Erro de sintaxe Java");
        System.out.println("OK: sintaxe de " + fontes.size() + " arquivos Java analisada (sem compilacao Spring).");
    }
}
