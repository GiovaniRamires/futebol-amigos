package com.futebolamigos.service;

import com.futebolamigos.dto.CadastroPublicoFormDto;
import com.futebolamigos.dto.JogadorFormDto;
import com.futebolamigos.exception.RegraNegocioException;
import com.futebolamigos.model.TipoJogador;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UsuarioServiceTest {

    @Autowired
    private UsuarioService usuarioService;

    @Test
    void deveCadastrarJogadorComSenhaCriptografada() {
        JogadorFormDto form = JogadorFormDto.builder()
                .nome("Jogador Teste")
                .email("jogador.teste@example.com")
                .senha("senha123")
                .tipoJogador(TipoJogador.MENSALISTA)
                .ativo(true)
                .build();

        var usuario = usuarioService.cadastrar(form);

        assertThat(usuario.getId()).isNotNull();
        assertThat(usuario.getSenhaHash()).isNotEqualTo("senha123");
        assertThat(usuario.getPerfil().name()).isEqualTo("JOGADOR");
    }

    @Test
    void naoDeveCadastrarSemSenha() {
        JogadorFormDto form = JogadorFormDto.builder()
                .nome("Sem Senha")
                .email("sem.senha@example.com")
                .tipoJogador(TipoJogador.AVULSO)
                .ativo(true)
                .build();

        assertThatThrownBy(() -> usuarioService.cadastrar(form))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Senha");
    }

    @Test
    void naoDeveCadastrarEmailDuplicado() {
        JogadorFormDto primeiro = JogadorFormDto.builder()
                .nome("Primeiro")
                .email("duplicado@example.com")
                .senha("senha123")
                .tipoJogador(TipoJogador.MENSALISTA)
                .ativo(true)
                .build();
        usuarioService.cadastrar(primeiro);

        JogadorFormDto duplicado = JogadorFormDto.builder()
                .nome("Segundo")
                .email("duplicado@example.com")
                .senha("outrasenha")
                .tipoJogador(TipoJogador.AVULSO)
                .ativo(true)
                .build();

        assertThatThrownBy(() -> usuarioService.cadastrar(duplicado))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("e-mail");
    }

    @Test
    void naoDeveDesativarUnicoAdministradorAtivo() {
        Long idUnicoAdmin = usuarioService.listarTodos().stream()
                .filter(u -> u.getPerfil().name().equals("ADMINISTRADOR"))
                .findFirst()
                .orElseThrow()
                .getId();

        assertThatThrownBy(() -> usuarioService.desativar(idUnicoAdmin))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("administrador");
    }

    @Test
    void cadastroViaLinkPublicoEntraSempreComoJogadorAvulsoAtivo() {
        CadastroPublicoFormDto form = CadastroPublicoFormDto.builder()
                .nome("Autocadastrado")
                .email("autocadastrado@example.com")
                .telefone("11999990000")
                .senha("senha123")
                .build();

        var usuario = usuarioService.cadastrarViaLinkPublico(form);

        assertThat(usuario.getId()).isNotNull();
        assertThat(usuario.getPerfil().name()).isEqualTo("JOGADOR");
        assertThat(usuario.getTipoJogador()).isEqualTo(TipoJogador.AVULSO);
        assertThat(usuario.isAtivo()).isTrue();
        assertThat(usuario.getSenhaHash()).isNotEqualTo("senha123");
    }

    @Test
    void cadastroViaLinkPublicoNaoPermiteEmailDuplicado() {
        CadastroPublicoFormDto primeiro = CadastroPublicoFormDto.builder()
                .nome("Primeiro Publico")
                .email("publico.duplicado@example.com")
                .senha("senha123")
                .build();
        usuarioService.cadastrarViaLinkPublico(primeiro);

        CadastroPublicoFormDto duplicado = CadastroPublicoFormDto.builder()
                .nome("Segundo Publico")
                .email("publico.duplicado@example.com")
                .senha("outrasenha")
                .build();

        assertThatThrownBy(() -> usuarioService.cadastrarViaLinkPublico(duplicado))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("e-mail");
    }
}
