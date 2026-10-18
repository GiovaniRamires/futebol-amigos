package com.futebolamigos.dto;

import com.futebolamigos.model.TipoJogador;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Formulario de cadastro/edicao de jogador, usado pelo administrador.
 * A senha e opcional na edicao (mantem a atual se em branco).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JogadorFormDto {

    private Long id;

    @NotBlank(message = "Informe o nome")
    private String nome;

    @NotBlank(message = "Informe o e-mail")
    @Email(message = "E-mail invalido")
    private String email;

    private String telefone;

    /** Opcional na edicao; obrigatoria no cadastro (validado no service). */
    private String senha;

    @NotNull(message = "Informe o tipo de jogador")
    private TipoJogador tipoJogador;

    private boolean ativo = true;
}
