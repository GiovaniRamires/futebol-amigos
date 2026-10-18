package com.futebolamigos.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Formulario de configuracao global do sistema (mensalidade e Pix).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracaoFormDto {

    @NotNull(message = "Informe o valor da mensalidade")
    @Positive(message = "O valor deve ser maior que zero")
    private BigDecimal valorMensalidade;

    @NotNull(message = "Informe o dia de vencimento")
    @Min(value = 1, message = "O dia deve ser entre 1 e 28")
    @Max(value = 28, message = "O dia deve ser entre 1 e 28")
    private Integer diaVencimentoMensalidade;

    private String chavePix;

    private String nomeBeneficiarioPix;

    private String instrucoesPagamento;
}
