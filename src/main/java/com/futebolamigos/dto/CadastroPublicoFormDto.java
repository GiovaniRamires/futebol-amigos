package com.futebolamigos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Formulario de autocadastro publico via link com token (ver
 * {@link com.futebolamigos.controller.CadastroPublicoController}).
 * Nao permite escolher perfil, tipo de jogador ou estado ativo -
 * essas decisoes ficam sempre a cargo do administrador apos o cadastro.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CadastroPublicoFormDto {

    @NotBlank(message = "Informe o nome")
    private String nome;

    @NotBlank(message = "Informe o e-mail")
    @Email(message = "E-mail invalido")
    private String email;

    private String telefone;

    @NotBlank(message = "Informe uma senha")
    @Size(min = 6, message = "A senha deve ter ao menos 6 caracteres")
    private String senha;
}
