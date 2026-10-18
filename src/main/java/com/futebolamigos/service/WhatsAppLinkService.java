package com.futebolamigos.service;

import com.futebolamigos.model.Cobranca;
import com.futebolamigos.model.ConfiguracaoSistema;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

/**
 * Monta o link wa.me com mensagem pre-formatada de cobranca. NAO envia nada
 * automaticamente - o administrador revisa e envia manualmente pelo WhatsApp
 * (ver docs/DECISIONS.md ADR-006).
 */
@Service
public class WhatsAppLinkService {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public String montarLinkCobranca(Cobranca cobranca, ConfiguracaoSistema configuracao, String telefoneE164SemSimbolos) {
        String mensagem = montarMensagem(cobranca, configuracao);
        String mensagemCodificada = URLEncoder.encode(mensagem, StandardCharsets.UTF_8);
        String base = "https://wa.me/" + (telefoneE164SemSimbolos == null ? "" : telefoneE164SemSimbolos);
        return base + "?text=" + mensagemCodificada;
    }

    private String montarMensagem(Cobranca cobranca, ConfiguracaoSistema configuracao) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ola, ").append(cobranca.getUsuario().getNome()).append("! ");
        sb.append("Segue a cobranca referente a ");
        sb.append(cobranca.getTipo() == com.futebolamigos.model.TipoCobranca.MENSALIDADE ? "mensalidade" : "diaria");
        sb.append(" (competencia ").append(cobranca.getCompetencia()).append("):\n");
        sb.append("Valor: R$ ").append(cobranca.getValor()).append("\n");
        sb.append("Vencimento: ").append(cobranca.getVencimento().format(FORMATO_DATA)).append("\n");
        if (configuracao.getChavePix() != null && !configuracao.getChavePix().isBlank()) {
            sb.append("Chave Pix: ").append(configuracao.getChavePix()).append("\n");
        }
        if (configuracao.getNomeBeneficiarioPix() != null && !configuracao.getNomeBeneficiarioPix().isBlank()) {
            sb.append("Beneficiario: ").append(configuracao.getNomeBeneficiarioPix()).append("\n");
        }
        if (configuracao.getInstrucoesPagamento() != null && !configuracao.getInstrucoesPagamento().isBlank()) {
            sb.append(configuracao.getInstrucoesPagamento()).append("\n");
        }
        sb.append("Obrigado!");
        return sb.toString();
    }
}
