package com.futebolamigos.controller;

import com.futebolamigos.model.Usuario;
import com.futebolamigos.service.CobrancaService;
import com.futebolamigos.service.PagamentoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Consulta das proprias cobrancas e pagamentos, do ponto de vista do jogador.
 */
@Controller
public class FinanceiroJogadorController {

    private final CobrancaService cobrancaService;
    private final PagamentoService pagamentoService;

    public FinanceiroJogadorController(CobrancaService cobrancaService, PagamentoService pagamentoService) {
        this.cobrancaService = cobrancaService;
        this.pagamentoService = pagamentoService;
    }

    @GetMapping("/jogador/financeiro")
    public String minhasCobrancas(@AuthenticationPrincipal Usuario usuario, Model model) {
        var cobrancas = cobrancaService.listarPorUsuario(usuario.getId());
        model.addAttribute("cobrancas", cobrancas);
        return "jogador/financeiro";
    }
}
