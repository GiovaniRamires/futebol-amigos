package com.futebolamigos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Confirmacao (ou cancelamento) de presenca de um jogador em uma partida,
 * feita pelo proprio jogador. NAO torna o jogador elegivel para o sorteio
 * por si so - ver {@link SelecaoSorteio}.
 */
@Entity
@Table(name = "confirmacao_presenca",
        uniqueConstraints = @UniqueConstraint(name = "uk_confirmacao_partida_usuario", columnNames = {"partida_id", "usuario_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmacaoPresenca {

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

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusConfirmacao status;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status_comparecimento", nullable = false, length = 20)
    private StatusComparecimento statusComparecimento = StatusComparecimento.NAO_REGISTRADO;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private Instant criadoEm = Instant.now();

    @Builder.Default
    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm = Instant.now();
}
