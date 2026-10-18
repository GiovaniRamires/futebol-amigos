package com.futebolamigos.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Login e redirecionamento pos-autenticacao conforme o perfil do usuario.
 */
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/acesso-negado")
    public String acessoNegado() {
        return "erro/acesso-negado";
    }

    /** Ponto unico pos-login: decide o painel certo conforme o perfil. */
    @GetMapping("/painel")
    public String painel(Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMINISTRADOR"));
        return admin ? "redirect:/admin/dashboard" : "redirect:/jogador/painel";
    }
}
