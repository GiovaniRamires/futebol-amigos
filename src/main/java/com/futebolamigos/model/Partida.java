package com.futebolamigos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Uma partida (jogo) organizada pelo administrador.
 */
@Entity
@Table(name = "partida")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Partida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @NotBlank
    @Column(nullable = false, length = 160)
    private String local;

    @NotNull
    @Positive
    @Column(name = "limite_participantes", nullable = false)
    private Integer limiteParticipantes;

    @NotNull
    @Column(name = "valor_diaria", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDiaria;

    @Builder.Default
    @Column(nullable = false)
    private boolean cancelada = false;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private Instant criadoEm = Instant.now();
}
