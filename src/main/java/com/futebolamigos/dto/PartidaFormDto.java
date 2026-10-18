package com.futebolamigos.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Formulario de criacao/edicao de partida, usado pelo administrador.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartidaFormDto {

    private Long id;

    @NotNull(message = "Informe a data e horario")
    @FutureOrPresent(message = "A data da partida nao pode ser no passado")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dataHora;

    @NotBlank(message = "Informe o local")
    private String local;

    @NotNull(message = "Informe o limite de participantes")
    @Positive(message = "O limite deve ser maior que zero")
    private Integer limiteParticipantes;

    @NotNull(message = "Informe o valor da diaria")
    @Positive(message = "O valor da diaria deve ser maior que zero")
    private BigDecimal valorDiaria;
}
