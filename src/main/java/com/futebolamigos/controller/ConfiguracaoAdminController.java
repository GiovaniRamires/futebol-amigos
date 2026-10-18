package com.futebolamigos.controller;

import com.futebolamigos.dto.ConfiguracaoFormDto;
import com.futebolamigos.model.ConfiguracaoSistema;
import com.futebolamigos.service.ConfiguracaoSistemaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Configuracoes financeiras do sistema (mensalidade e Pix) e gestao do link
 * publico de autocadastro de jogadores.
 */
@Controller
public class ConfiguracaoAdminController {

    private final ConfiguracaoSistemaService configuracaoSistemaService;

    public ConfiguracaoAdminController(ConfiguracaoSistemaService configuracaoSistemaService) {
        this.configuracaoSistemaService = configuracaoSistemaService;
    }

    @GetMapping("/admin/configuracoes")
    public String tela(Model model, HttpServletRequest request) {
        ConfiguracaoSistema atual = configuracaoSistemaService.buscar();
        model.addAttribute("form", ConfiguracaoFormDto.builder()
                .valorMensalidade(atual.getValorMensalidade())
                .diaVencimentoMensalidade(atual.getDiaVencimentoMensalidade())
                .chavePix(atual.getChavePix())
                .nomeBeneficiarioPix(atual.getNomeBeneficiarioPix())
                .instrucoesPagamento(atual.getInstrucoesPagamento())
                .build());
        model.addAttribute("linkCadastro", montarLinkCadastro(request, configuracaoSistemaService.obterOuGerarTokenCadastro()));
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

    @PostMapping("/admin/configuracoes/link-cadastro/regenerar")
    public String regenerarLinkCadastro() {
        configuracaoSistemaService.regenerarTokenCadastro();
        return "redirect:/admin/configuracoes";
    }

    private String montarLinkCadastro(HttpServletRequest request, String token) {
        return ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath("/cadastro")
                .replaceQuery(null)
                .queryParam("token", token)
                .build()
                .toUriString();
    }
}
