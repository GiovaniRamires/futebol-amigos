package com.futebolamigos.model;

/**
 * Resultado do comparecimento efetivo de um jogador a uma partida,
 * registrado pelo administrador. Independente da confirmacao previa.
 */
public enum StatusComparecimento {
    NAO_REGISTRADO,
    COMPARECEU,
    FALTOU,
    ATRASADO
}
