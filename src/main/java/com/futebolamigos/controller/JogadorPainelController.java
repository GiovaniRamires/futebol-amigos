package com.futebolamigos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Painel do jogador (visao geral). Conteudo expandido nas proximas etapas
 * (proxima partida, confirmacao de presenca, times publicados, financeiro).
 */
@Controller
public class JogadorPainelController {

    @GetMapping("/jogador/painel")
    public String painel() {
        return "jogador/painel";
    }
}
