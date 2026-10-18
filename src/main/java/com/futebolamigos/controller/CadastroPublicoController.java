package com.futebolamigos.controller;

import com.futebolamigos.dto.CadastroPublicoFormDto;
import com.futebolamigos.exception.RegraNegocioException;
import com.futebolamigos.service.ConfiguracaoSistemaService;
import com.futebolamigos.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Autocadastro publico de jogadores via link com token
 * (gerado e revogavel pelo administrador em /admin/configuracoes).
 *
 * O jogador entra sempre como JOGADOR/AVULSO/ativo; a promocao a mensalista
 * e qualquer outra alteracao ficam a cargo do administrador depois
 * (ver {@link UsuarioService#cadastrarViaLinkPublico}).
 */
@Controller
public class CadastroPublicoController {

    private final ConfiguracaoSistemaService configuracaoSistemaService;
    private final UsuarioService usuarioService;

    public CadastroPublicoController(ConfiguracaoSistemaService configuracaoSistemaService,
                                      UsuarioService usuarioService) {
        this.configuracaoSistemaService = configuracaoSistemaService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/cadastro")
    public String formulario(@RequestParam(required = false) String token, Model model) {
        if (!configuracaoSistemaService.tokenCadastroValido(token)) {
            model.addAttribute("tokenInvalido", true);
            return "publico/cadastro";
        }
        model.addAttribute("token", token);
        model.addAttribute("form", new CadastroPublicoFormDto());
        return "publico/cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@RequestParam String token,
                             @Valid @ModelAttribute("form") CadastroPublicoFormDto form,
                             BindingResult bindingResult,
                             Model model) {
        if (!configuracaoSistemaService.tokenCadastroValido(token)) {
            model.addAttribute("tokenInvalido", true);
            return "publico/cadastro";
        }
        model.addAttribute("token", token);
        if (bindingResult.hasErrors()) {
            return "publico/cadastro";
        }
        try {
            usuarioService.cadastrarViaLinkPublico(form);
        } catch (RegraNegocioException ex) {
            model.addAttribute("erroNegocio", ex.getMessage());
            return "publico/cadastro";
        }
        return "redirect:/login?cadastrado";
    }
}
