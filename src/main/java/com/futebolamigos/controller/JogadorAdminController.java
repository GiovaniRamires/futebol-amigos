package com.futebolamigos.controller;

import com.futebolamigos.dto.JogadorFormDto;
import com.futebolamigos.model.Usuario;
import com.futebolamigos.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Gestao de jogadores pelo administrador: cadastrar, editar, ativar, desativar.
 */
@Controller
@RequestMapping("/admin/jogadores")
public class JogadorAdminController {

    private final UsuarioService usuarioService;

    public JogadorAdminController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("jogadores", usuarioService.listarTodos());
        return "admin/jogadores/lista";
    }

    @GetMapping("/novo")
    public String novoFormulario(Model model) {
        model.addAttribute("form", JogadorFormDto.builder().ativo(true).build());
        return "admin/jogadores/formulario";
    }

    @PostMapping
    public String cadastrar(@Valid @ModelAttribute("form") JogadorFormDto form, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/jogadores/formulario";
        }
        usuarioService.cadastrar(form);
        return "redirect:/admin/jogadores";
    }

    @GetMapping("/{id}/editar")
    public String editarFormulario(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id);
        JogadorFormDto form = JogadorFormDto.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .telefone(usuario.getTelefone())
                .tipoJogador(usuario.getTipoJogador())
                .ativo(usuario.isAtivo())
                .build();
        model.addAttribute("form", form);
        return "admin/jogadores/formulario";
    }

    @PostMapping("/{id}")
    public String editar(@PathVariable Long id, @Valid @ModelAttribute("form") JogadorFormDto form, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/jogadores/formulario";
        }
        usuarioService.editar(id, form);
        return "redirect:/admin/jogadores";
    }

    @PostMapping("/{id}/ativar")
    public String ativar(@PathVariable Long id) {
        usuarioService.ativar(id);
        return "redirect:/admin/jogadores";
    }

    @PostMapping("/{id}/desativar")
    public String desativar(@PathVariable Long id) {
        usuarioService.desativar(id);
        return "redirect:/admin/jogadores";
    }
}
