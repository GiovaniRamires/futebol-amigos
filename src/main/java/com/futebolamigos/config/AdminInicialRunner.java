package com.futebolamigos.config;

import com.futebolamigos.model.Perfil;
import com.futebolamigos.model.TipoJogador;
import com.futebolamigos.model.Usuario;
import com.futebolamigos.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Cria o primeiro administrador no boot, caso nenhum ADMINISTRADOR exista
 * ainda no banco. Usa app.admin.email / app.admin.senha (variaveis de
 * ambiente ADMIN_EMAIL / ADMIN_SENHA); se a senha nao for informada, gera
 * uma senha aleatoria e a imprime UMA UNICA VEZ no log de inicializacao.
 */
@Component
public class AdminInicialRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicialRunner.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminNome;
    private final String adminEmail;
    private final String adminSenhaConfigurada;

    public AdminInicialRunner(UsuarioRepository usuarioRepository,
                               PasswordEncoder passwordEncoder,
                               @Value("${app.admin.nome}") String adminNome,
                               @Value("${app.admin.email}") String adminEmail,
                               @Value("${app.admin.senha}") String adminSenhaConfigurada) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminNome = adminNome;
        this.adminEmail = adminEmail;
        this.adminSenhaConfigurada = adminSenhaConfigurada;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean existeAdmin = usuarioRepository.findAllByOrderByNomeAsc().stream()
                .anyMatch(u -> u.getPerfil() == Perfil.ADMINISTRADOR);
        if (existeAdmin) {
            return;
        }
        if (usuarioRepository.existsByEmail(adminEmail)) {
            log.warn("Email de administrador padrao ja esta em uso por um jogador existente; nenhum administrador inicial foi criado automaticamente.");
            return;
        }

        String senhaPlana = (adminSenhaConfigurada == null || adminSenhaConfigurada.isBlank())
                ? gerarSenhaAleatoria()
                : adminSenhaConfigurada;

        Usuario admin = Usuario.builder()
                .nome(adminNome)
                .email(adminEmail)
                .senhaHash(passwordEncoder.encode(senhaPlana))
                .perfil(Perfil.ADMINISTRADOR)
                .tipoJogador(TipoJogador.MENSALISTA)
                .ativo(true)
                .build();
        usuarioRepository.save(admin);

        if (adminSenhaConfigurada == null || adminSenhaConfigurada.isBlank()) {
            log.warn("=============================================================");
            log.warn(" Administrador inicial criado: {}", adminEmail);
            log.warn(" Senha gerada automaticamente (anote agora, nao sera exibida de novo): {}", senhaPlana);
            log.warn(" Defina ADMIN_SENHA no ambiente para evitar este aviso no futuro.");
            log.warn("=============================================================");
        } else {
            log.info("Administrador inicial criado: {}", adminEmail);
        }
    }

    private String gerarSenhaAleatoria() {
        byte[] bytes = new byte[12];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
