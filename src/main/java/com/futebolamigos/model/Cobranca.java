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
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Cobranca financeira de um jogador: mensalidade (por competencia) ou
 * diaria (por partida, gerada apenas apos comparecimento de avulso).
 */
@Entity
@Table(name = "cobranca",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cobranca_usuario_tipo_competencia_partida",
                columnNames = {"usuario_id", "tipo", "competencia", "partida_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cobranca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoCobranca tipo;

    /**
     * Competencia no formato AAAA-MM (mes/ano), usada para MENSALIDADE.
     * Para DIARIA, armazena o mes/ano da data da partida (apenas para
     * compor a chave de unicidade de forma consistente).
     */
    @NotNull
    @Column(nullable = false, length = 7)
    private String competencia;

    /**
     * Partida associada, apenas para cobrancas do tipo DIARIA. Nulo para
     * MENSALIDADE. Faz parte da chave de unicidade para permitir multiplas
     * diarias no mesmo mes (uma por partida).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_id")
    private Partida partida;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @NotNull
    @Column(name = "vencimento", nullable = false)
    private LocalDate vencimento;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusCobranca status = StatusCobranca.PENDENTE;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private Instant criadoEm = Instant.now();

    public static String competenciaDe(YearMonth yearMonth) {
        return yearMonth.toString(); // AAAA-MM
    }
}
