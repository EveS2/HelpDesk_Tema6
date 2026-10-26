package br.edu.helpdesk.exception;

import org.springframework.http.HttpStatus;

public class RegraException extends RuntimeException {
    public final HttpStatus status;
    public RegraException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }
    public static RegraException naoEncontrado(String nome) {
        return new RegraException(HttpStatus.NOT_FOUND, nome + " nao encontrado(a).");
    }
    public static RegraException proibido() {
        return new RegraException(HttpStatus.FORBIDDEN, "Voce nao pode executar esta acao.");
    }
    public static RegraException conflito(String mensagem) {
        return new RegraException(HttpStatus.CONFLICT, mensagem);
    }
}
