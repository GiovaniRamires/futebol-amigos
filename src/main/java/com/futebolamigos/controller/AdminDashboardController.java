package com.futebolamigos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Dashboard administrativo (visao geral). Conteudo expandido nas proximas etapas
 * (proximas partidas, pendencias financeiras, aprovacoes).
 */
@Controller
public class AdminDashboardController {

    @GetMapping("/admin/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }
}
