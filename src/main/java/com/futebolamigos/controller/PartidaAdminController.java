package com.futebolamigos.controller;

import com.futebolamigos.dto.PartidaFormDto;
import com.futebolamigos.model.Partida;
import com.futebolamigos.service.PartidaService;
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
 * Gestao de partidas pelo administrador.
 */
@Controller
@RequestMapping("/admin/partidas")
public class PartidaAdminController {

    private final PartidaService partidaService;

    public PartidaAdminController(PartidaService partidaService) {
        this.partidaService = partidaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("partidas", partidaService.listarTodas());
        return "admin/partidas/lista";
    }

    @GetMapping("/novo")
    public String novoFormulario(Model model) {
        model.addAttribute("form", PartidaFormDto.builder().build());
        return "admin/partidas/formulario";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("form") PartidaFormDto form, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/partidas/formulario";
        }
        Partida partida = partidaService.criar(form);
        return "redirect:/admin/partidas/" + partida.getId();
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("partida", partidaService.buscarPorId(id));
        return "admin/partidas/detalhe";
    }

    @GetMapping("/{id}/editar")
    public String editarFormulario(@PathVariable Long id, Model model) {
        Partida partida = partidaService.buscarPorId(id);
        PartidaFormDto form = PartidaFormDto.builder()
                .id(partida.getId())
                .dataHora(partida.getDataHora())
                .local(partida.getLocal())
                .limiteParticipantes(partida.getLimiteParticipantes())
                .valorDiaria(partida.getValorDiaria())
                .build();
        model.addAttribute("form", form);
        return "admin/partidas/formulario";
    }

    @PostMapping("/{id}")
    public String editar(@PathVariable Long id, @Valid @ModelAttribute("form") PartidaFormDto form, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/partidas/formulario";
        }
        partidaService.editar(id, form);
        return "redirect:/admin/partidas/" + id;
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id) {
        partidaService.cancelar(id);
        return "redirect:/admin/partidas";
    }
}
