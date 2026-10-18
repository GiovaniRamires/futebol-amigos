package com.futebolamigos.controller;

import com.futebolamigos.service.SorteioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Sorteio e publicacao dos times para uma partida.
 */
@Controller
@RequestMapping("/admin/partidas/{partidaId}/sorteio")
public class SorteioAdminController {

    private final SorteioService sorteioService;

    public SorteioAdminController(SorteioService sorteioService) {
        this.sorteioService = sorteioService;
    }

    @GetMapping
    public String tela(@PathVariable Long partidaId, Model model) {
        model.addAttribute("partidaId", partidaId);
        model.addAttribute("historico", sorteioService.historicoPorPartida(partidaId));
        return "admin/partidas/sorteio";
    }

    @PostMapping("/sortear")
    public String sortear(@PathVariable Long partidaId) {
        sorteioService.sortear(partidaId);
        return "redirect:/admin/partidas/" + partidaId + "/sorteio";
    }

    @PostMapping("/refazer")
    public String refazer(@PathVariable Long partidaId) {
        sorteioService.refazer(partidaId);
        return "redirect:/admin/partidas/" + partidaId + "/sorteio";
    }

    @PostMapping("/{sorteioId}/publicar")
    public String publicar(@PathVariable Long partidaId, @PathVariable Long sorteioId) {
        sorteioService.publicar(sorteioId);
        return "redirect:/admin/partidas/" + partidaId + "/sorteio";
    }
}
