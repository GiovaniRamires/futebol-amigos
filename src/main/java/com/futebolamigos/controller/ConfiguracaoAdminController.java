package com.futebolamigos.controller;

import com.futebolamigos.dto.ConfiguracaoFormDto;
import com.futebolamigos.model.ConfiguracaoSistema;
import com.futebolamigos.service.ConfiguracaoSistemaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Configuracoes financeiras do sistema (mensalidade e Pix).
 */
@Controller
public class ConfiguracaoAdminController {

    private final ConfiguracaoSistemaService configuracaoSistemaService;

    public ConfiguracaoAdminController(ConfiguracaoSistemaService configuracaoSistemaService) {
        this.configuracaoSistemaService = configuracaoSistemaService;
    }

    @GetMapping("/admin/configuracoes")
    public String tela(Model model) {
        ConfiguracaoSistema atual = configuracaoSistemaService.buscar();
        model.addAttribute("form", ConfiguracaoFormDto.builder()
                .valorMensalidade(atual.getValorMensalidade())
                .diaVencimentoMensalidade(atual.getDiaVencimentoMensalidade())
                .chavePix(atual.getChavePix())
                .nomeBeneficiarioPix(atual.getNomeBeneficiarioPix())
                .instrucoesPagamento(atual.getInstrucoesPagamento())
                .build());
        return "admin/configuracoes";
    }

    @PostMapping("/admin/configuracoes")
    public String salvar(@Valid @ModelAttribute("form") ConfiguracaoFormDto form, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/configuracoes";
        }
        ConfiguracaoSistema novaConfiguracao = ConfiguracaoSistema.builder()
                .valorMensalidade(form.getValorMensalidade())
                .diaVencimentoMensalidade(form.getDiaVencimentoMensalidade())
                .chavePix(form.getChavePix())
                .nomeBeneficiarioPix(form.getNomeBeneficiarioPix())
                .instrucoesPagamento(form.getInstrucoesPagamento())
                .build();
        configuracaoSistemaService.atualizar(novaConfiguracao);
        return "redirect:/admin/configuracoes";
    }
}
