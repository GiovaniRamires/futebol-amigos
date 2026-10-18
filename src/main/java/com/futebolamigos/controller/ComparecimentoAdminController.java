package com.futebolamigos.controller;

import com.futebolamigos.model.StatusComparecimento;
import com.futebolamigos.service.ConfirmacaoPresencaService;
import com.futebolamigos.service.PartidaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Registro de comparecimento efetivo (compareceu/faltou/atrasado) pelo
 * administrador - distinto da confirmacao previa feita pelo jogador.
 */
@Controller
@RequestMapping("/admin/partidas/{partidaId}/comparecimento")
public class ComparecimentoAdminController {

    private final ConfirmacaoPresencaService confirmacaoPresencaService;
    private final PartidaService partidaService;

    public ComparecimentoAdminController(ConfirmacaoPresencaService confirmacaoPresencaService,
                                          PartidaService partidaService) {
        this.confirmacaoPresencaService = confirmacaoPresencaService;
        this.partidaService = partidaService;
    }

    @PostMapping("/{usuarioId}")
    public String registrar(@PathVariable Long partidaId,
                             @PathVariable Long usuarioId,
                             @RequestParam StatusComparecimento status,
                             Model model) {
        confirmacaoPresencaService.registrarComparecimento(partidaId, usuarioId, status);
        return "redirect:/admin/partidas/" + partidaId;
    }
}
