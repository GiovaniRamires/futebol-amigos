package com.futebolamigos.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * Tratamento global de excecoes para os controllers MVC (Thymeleaf).
 * Renderiza uma pagina de erro amigavel em vez de vazar stacktraces.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ModelAndView tratarNaoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest request) {
        log.warn("Recurso nao encontrado em {}: {}", request.getRequestURI(), ex.getMessage());
        ModelAndView mv = new ModelAndView("erro/erro-generico");
        mv.setStatus(org.springframework.http.HttpStatus.NOT_FOUND);
        mv.addObject("mensagem", ex.getMessage());
        return mv;
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ModelAndView tratarRegraNegocio(RegraNegocioException ex, HttpServletRequest request) {
        log.warn("Regra de negocio violada em {}: {}", request.getRequestURI(), ex.getMessage());
        ModelAndView mv = new ModelAndView("erro/erro-generico");
        mv.setStatus(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY);
        mv.addObject("mensagem", ex.getMessage());
        return mv;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ModelAndView tratarIntegridade(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Violacao de integridade em {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        ModelAndView mv = new ModelAndView("erro/erro-generico");
        mv.setStatus(org.springframework.http.HttpStatus.CONFLICT);
        mv.addObject("mensagem", "Operacao invalida: registro duplicado ou referenciado por outro dado.");
        return mv;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView tratarGenerico(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado em {}", request.getRequestURI(), ex);
        ModelAndView mv = new ModelAndView("erro/erro-generico");
        mv.setStatus(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        mv.addObject("mensagem", "Ocorreu um erro inesperado. Tente novamente.");
        return mv;
    }
}
