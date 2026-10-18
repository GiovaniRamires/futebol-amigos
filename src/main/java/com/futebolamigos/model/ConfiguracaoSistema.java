package com.futebolamigos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Configuracao global do sistema (singleton, id fixo = 1L).
 * Guarda valor da mensalidade, dia de vencimento e dados de Pix.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracaoSistema {

    public static final long ID_UNICO = 1L;

    @Id
    @Builder.Default
    private Long id = ID_UNICO;

    @NotNull
    @Positive
    @Column(name = "valor_mensalidade", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorMensalidade;

    @NotNull
    @Column(name = "dia_vencimento_mensalidade", nullable = false)
    private Integer diaVencimentoMensalidade;

    @Column(name = "chave_pix", length = 160)
    private String chavePix;

    @Column(name = "nome_beneficiario_pix", length = 160)
    private String nomeBeneficiarioPix;

    @Column(name = "instrucoes_pagamento", length = 500)
    private String instrucoesPagamento;

    /**
     * Token do link publico de autocadastro de jogadores (/cadastro?token=...).
     * Reutilizavel enquanto nao for regenerado pelo administrador; nulo ate a
     * primeira geracao (ver {@link com.futebolamigos.service.ConfiguracaoSistemaService}).
     */
    @Column(name = "token_cadastro", length = 40)
    private String tokenCadastro;
}
