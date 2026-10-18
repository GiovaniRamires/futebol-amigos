package com.futebolamigos.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.futebolamigos.model.MetodoPagamento;

/**
 * Formulario de registro de pagamento pelo administrador.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagamentoFormDto {

    @NotNull(message = "Informe o valor pago")
    @Positive(message = "O valor deve ser maior que zero")
    private BigDecimal valor;

    @NotNull(message = "Informe a data do pagamento")
    private LocalDate dataPagamento;

    @NotNull(message = "Informe o metodo de pagamento")
    private MetodoPagamento metodo;
}
