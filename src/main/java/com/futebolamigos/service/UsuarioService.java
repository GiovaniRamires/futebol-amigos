package com.futebolamigos.service;

import com.futebolamigos.dto.CadastroPublicoFormDto;
import com.futebolamigos.dto.JogadorFormDto;
import com.futebolamigos.dto.UsuarioViewDto;
import com.futebolamigos.exception.RecursoNaoEncontradoException;
import com.futebolamigos.exception.RegraNegocioException;
import com.futebolamigos.model.Perfil;
import com.futebolamigos.model.TipoJogador;
import com.futebolamigos.model.Usuario;
import com.futebolamigos.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Regras de negocio de cadastro e gestao de jogadores/administradores.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioViewDto> listarTodos() {
        return usuarioRepository.findAllByOrderByNomeAsc().stream()
                .map(UsuarioViewDto::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioViewDto> listarAtivos() {
        return usuarioRepository.findAllByAtivoTrueOrderByNomeAsc().stream()
                .map(UsuarioViewDto::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Jogador nao encontrado: id " + id));
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado: " + email));
    }

    @Transactional
    public Usuario cadastrar(JogadorFormDto form) {
        if (form.getSenha() == null || form.getSenha().isBlank()) {
            throw new RegraNegocioException("Senha e obrigatoria no cadastro de um novo jogador.");
        }
        if (usuarioRepository.existsByEmail(form.getEmail())) {
            throw new RegraNegocioException("Ja existe um usuario cadastrado com este e-mail.");
        }
        Usuario usuario = Usuario.builder()
                .nome(form.getNome())
                .email(form.getEmail())
                .telefone(form.getTelefone())
                .senhaHash(passwordEncoder.encode(form.getSenha()))
                .perfil(Perfil.JOGADOR)
                .tipoJogador(form.getTipoJogador())
                .ativo(form.isAtivo())
                .build();
        return usuarioRepository.save(usuario);
    }

    /**
     * Autocadastro via link publico (/cadastro?token=...). O jogador define
     * apenas nome, e-mail, telefone e senha; perfil, tipo e ativacao sao
     * sempre fixos (JOGADOR, AVULSO, ativo) - a promocao a mensalista fica
     * a cargo do administrador, pois impacta a geracao de cobrancas.
     */
    @Transactional
    public Usuario cadastrarViaLinkPublico(CadastroPublicoFormDto form) {
        if (usuarioRepository.existsByEmail(form.getEmail())) {
            throw new RegraNegocioException("Ja existe um usuario cadastrado com este e-mail.");
        }
        Usuario usuario = Usuario.builder()
                .nome(form.getNome())
                .email(form.getEmail())
                .telefone(form.getTelefone())
                .senhaHash(passwordEncoder.encode(form.getSenha()))
                .perfil(Perfil.JOGADOR)
                .tipoJogador(TipoJogador.AVULSO)
                .ativo(true)
                .build();
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario editar(Long id, JogadorFormDto form) {
        Usuario usuario = buscarPorId(id);
        if (usuarioRepository.existsByEmailAndIdNot(form.getEmail(), id)) {
            throw new RegraNegocioException("Ja existe outro usuario cadastrado com este e-mail.");
        }
        usuario.setNome(form.getNome());
        usuario.setEmail(form.getEmail());
        usuario.setTelefone(form.getTelefone());
        usuario.setTipoJogador(form.getTipoJogador());
        usuario.setAtivo(form.isAtivo());
        if (form.getSenha() != null && !form.getSenha().isBlank()) {
            usuario.setSenhaHash(passwordEncoder.encode(form.getSenha()));
        }
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void ativar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void desativar(Long id) {
        Usuario usuario = buscarPorId(id);
        if (usuario.getPerfil() == Perfil.ADMINISTRADOR) {
            long totalAdminsAtivos = usuarioRepository.findAllByAtivoTrueOrderByNomeAsc().stream()
                    .filter(u -> u.getPerfil() == Perfil.ADMINISTRADOR)
                    .count();
            if (totalAdminsAtivos <= 1) {
                throw new RegraNegocioException("Nao e possivel desativar o unico administrador ativo do sistema.");
            }
        }
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }
}
