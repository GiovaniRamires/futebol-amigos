package com.futebolamigos.controller;

import com.futebolamigos.service.SelecaoSorteioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Tela de selecao EXPLICITA de jogadores para o sorteio - a fonte unica de
 * elegibilidade (ver docs/DECISIONS.md ADR-003).
 */
@Controller
@RequestMapping("/admin/partidas/{partidaId}/selecao")
public class SelecaoSorteioAdminController {

    private final SelecaoSorteioService selecaoSorteioService;

    public SelecaoSorteioAdminController(SelecaoSorteioService selecaoSorteioService) {
        this.selecaoSorteioService = selecaoSorteioService;
    }

    @GetMapping
    public String tela(@PathVariable Long partidaId, Model model) {
        model.addAttribute("partidaId", partidaId);
        model.addAttribute("statusPresenca", selecaoSorteioService.statusPresencaParaSelecao(partidaId));
        model.addAttribute("selecionados", selecaoSorteioService.listarPorPartida(partidaId));
        return "admin/partidas/selecao";
    }

    @PostMapping("/{usuarioId}/incluir")
    public String incluir(@PathVariable Long partidaId,
                           @PathVariable Long usuarioId,
                           @RequestParam(required = false) String justificativaExcecao) {
        selecaoSorteioService.incluir(partidaId, usuarioId, justificativaExcecao);
        return "redirect:/admin/partidas/" + partidaId + "/selecao";
    }

    @PostMapping("/{usuarioId}/remover")
    public String remover(@PathVariable Long partidaId, @PathVariable Long usuarioId) {
        selecaoSorteioService.remover(partidaId, usuarioId);
        return "redirect:/admin/partidas/" + partidaId + "/selecao";
    }
}
