package com.futebolamigos.exception;

/**
 * Entidade de negocio nao encontrada (mapeada para HTTP 404).
 */
public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
