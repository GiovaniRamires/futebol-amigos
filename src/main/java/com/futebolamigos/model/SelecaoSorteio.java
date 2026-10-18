package com.futebolamigos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Selecao EXPLICITA, feita pelo administrador, de um jogador para participar
 * do sorteio de uma partida. Esta e' a UNICA fonte de elegibilidade para o
 * sorteio - a confirmacao de presenca, isoladamente, nunca e' suficiente.
 *
 * Permite incluir jogadores atrasados e, com justificativa obrigatoria,
 * jogadores que nao confirmaram presenca previamente.
 */
@Entity
@Table(name = "selecao_sorteio",
        uniqueConstraints = @UniqueConstraint(name = "uk_selecao_partida_usuario", columnNames = {"partida_id", "usuario_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelecaoSorteio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida partida;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /**
     * Preenchido somente quando o jogador e incluido sem confirmacao previa
     * (excecao). Obrigatorio nesse caso, validado na camada de servico.
     */
    @Column(name = "justificativa_excecao", length = 255)
    private String justificativaExcecao;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private Instant criadoEm = Instant.now();
}
