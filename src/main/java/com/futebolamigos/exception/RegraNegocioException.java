package com.futebolamigos.exception;

/**
 * Violacao de regra de negocio (mapeada para HTTP 422/400 conforme o caso).
 */
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
