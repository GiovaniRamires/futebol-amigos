package com.futebolamigos.controller;

import com.futebolamigos.dto.PagamentoFormDto;
import com.futebolamigos.model.Cobranca;
import com.futebolamigos.model.Usuario;
import com.futebolamigos.service.CobrancaService;
import com.futebolamigos.service.ConfiguracaoSistemaService;
import com.futebolamigos.service.PagamentoService;
import com.futebolamigos.service.WhatsAppLinkService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.YearMonth;

/**
 * Gestao financeira pelo administrador: gerar mensalidades, listar cobrancas,
 * registrar pagamentos, montar link do WhatsApp.
 */
@Controller
@RequestMapping("/admin/financeiro")
public class FinanceiroAdminController {

    private final CobrancaService cobrancaService;
    private final PagamentoService pagamentoService;
    private final ConfiguracaoSistemaService configuracaoSistemaService;
    private final WhatsAppLinkService whatsAppLinkService;

    public FinanceiroAdminController(CobrancaService cobrancaService,
                                      PagamentoService pagamentoService,
                                      ConfiguracaoSistemaService configuracaoSistemaService,
                                      WhatsAppLinkService whatsAppLinkService) {
        this.cobrancaService = cobrancaService;
        this.pagamentoService = pagamentoService;
        this.configuracaoSistemaService = configuracaoSistemaService;
        this.whatsAppLinkService = whatsAppLinkService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("cobrancas", cobrancaService.listarTodas());
        model.addAttribute("competenciaAtual", YearMonth.now().toString());
        return "admin/financeiro/lista";
    }

    @PostMapping("/mensalidades/gerar")
    public String gerarMensalidades(@RequestParam String competencia, Model model) {
        int criadas = cobrancaService.gerarMensalidadesDaCompetencia(YearMonth.parse(competencia));
        model.addAttribute("mensagem", criadas + " mensalidade(s) gerada(s) para a competencia " + competencia + ".");
        return "redirect:/admin/financeiro";
    }

    @GetMapping("/{cobrancaId}")
    public String detalhe(@PathVariable Long cobrancaId, Model model) {
        Cobranca cobranca = cobrancaService.buscarPorId(cobrancaId);
        model.addAttribute("cobranca", cobranca);
        model.addAttribute("saldoPendente", pagamentoService.calcularSaldoPendente(cobranca));
        model.addAttribute("pagamentos", pagamentoService.listarPorCobranca(cobrancaId));
        model.addAttribute("form", PagamentoFormDto.builder().build());

        String telefone = cobranca.getUsuario().getTelefone();
        if (telefone != null && !telefone.isBlank()) {
            String link = whatsAppLinkService.montarLinkCobranca(cobranca, configuracaoSistemaService.buscar(), telefone.replaceAll("\\D", ""));
            model.addAttribute("linkWhatsApp", link);
        }
        return "admin/financeiro/detalhe";
    }

    @PostMapping("/{cobrancaId}/pagamentos")
    public String registrarPagamento(@PathVariable Long cobrancaId,
                                      @Valid @ModelAttribute("form") PagamentoFormDto form,
                                      BindingResult bindingResult,
                                      @AuthenticationPrincipal Usuario admin) {
        if (bindingResult.hasErrors()) {
            return "admin/financeiro/detalhe";
        }
        pagamentoService.registrar(cobrancaId, form.getValor(), form.getDataPagamento(), form.getMetodo(), admin);
        return "redirect:/admin/financeiro/" + cobrancaId;
    }
}
