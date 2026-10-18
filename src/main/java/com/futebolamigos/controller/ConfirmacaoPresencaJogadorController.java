package com.futebolamigos.controller;

import com.futebolamigos.model.Usuario;
import com.futebolamigos.service.ConfirmacaoPresencaService;
import com.futebolamigos.service.PartidaService;
import com.futebolamigos.service.SorteioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Confirmacao de presenca e consulta de times, do ponto de vista do jogador.
 */
@Controller
public class ConfirmacaoPresencaJogadorController {

    private final ConfirmacaoPresencaService confirmacaoPresencaService;
    private final PartidaService partidaService;
    private final SorteioService sorteioService;

    public ConfirmacaoPresencaJogadorController(ConfirmacaoPresencaService confirmacaoPresencaService,
                                                 PartidaService partidaService,
                                                 SorteioService sorteioService) {
        this.confirmacaoPresencaService = confirmacaoPresencaService;
        this.partidaService = partidaService;
        this.sorteioService = sorteioService;
    }

    @GetMapping("/jogador/proxima-partida")
    public String proximaPartida(@AuthenticationPrincipal Usuario usuario, Model model) {
        partidaService.proximaPartida().ifPresent(partida -> {
            model.addAttribute("partida", partida);
            sorteioService.sorteioPublicado(partida.getId()).ifPresent(sorteio -> model.addAttribute("sorteio", sorteio));
        });
        return "jogador/proxima-partida";
    }

    @PostMapping("/jogador/partidas/{partidaId}/confirmar")
    public String confirmar(@PathVariable Long partidaId, @AuthenticationPrincipal Usuario usuario) {
        confirmacaoPresencaService.confirmar(partidaId, usuario.getId());
        return "redirect:/jogador/proxima-partida";
    }

    @PostMapping("/jogador/partidas/{partidaId}/cancelar")
    public String cancelar(@PathVariable Long partidaId, @AuthenticationPrincipal Usuario usuario) {
        confirmacaoPresencaService.cancelar(partidaId, usuario.getId());
        return "redirect:/jogador/proxima-partida";
    }
}
