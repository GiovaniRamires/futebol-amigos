package com.futebolamigos.dto;

import com.futebolamigos.model.Perfil;
import com.futebolamigos.model.TipoJogador;
import com.futebolamigos.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Representacao de usuario para exibicao em views, sem expor o hash da senha.
 */
@Getter
@Builder
@AllArgsConstructor
public class UsuarioViewDto {

    private Long id;
    private String nome;
    private String email;
    private Perfil perfil;
    private TipoJogador tipoJogador;
    private boolean ativo;

    public static UsuarioViewDto de(Usuario usuario) {
        return UsuarioViewDto.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .perfil(usuario.getPerfil())
                .tipoJogador(usuario.getTipoJogador())
                .ativo(usuario.isAtivo())
                .build();
    }
}
