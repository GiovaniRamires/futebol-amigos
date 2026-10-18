package com.futebolamigos.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cobre controle de acesso (briefing secao 10): paginas administrativas
 * protegidas, dados financeiros inacessiveis sem autenticacao.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ControleAcessoTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithAnonymousUser
    void deveRedirecionarParaLoginQuandoNaoAutenticado() throws Exception {
        mockMvc.perform(get("/painel"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithAnonymousUser
    void deveBloquearAcessoAPaginaAdministrativaSemAutenticacao() throws Exception {
        mockMvc.perform(get("/admin/jogadores"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void deveBloquearJogadorComumDeAcessarPaginaAdministrativa() throws Exception {
        mockMvc.perform(get("/admin/jogadores").with(user("jogador@example.com").roles("JOGADOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveBloquearAcessoAFinanceiroAdminSemPerfilAdministrador() throws Exception {
        mockMvc.perform(get("/admin/financeiro").with(user("jogador2@example.com").roles("JOGADOR")))
                .andExpect(status().isForbidden());
    }
}
