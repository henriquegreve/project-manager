package com.example.project_manager.model;

import com.example.project_manager.model.ProjectStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectStatusTest {

    @Test
    @DisplayName("Deve permitir transição EM_ANALISE -> ANALISE_REALIZADA")
    void shouldAllowTransitionFromEmAnaliseToAnaliseRealizada() {
        assertTrue(ProjectStatus.EM_ANALISE.canTransitionTo(ProjectStatus.ANALISE_REALIZADA));
    }

    @Test
    @DisplayName("Deve permitir transição ANALISE_REALIZADA -> ANALISE_APROVADA")
    void shouldAllowTransitionFromAnaliseRealizadaToAnaliseAprovada() {
        assertTrue(ProjectStatus.ANALISE_REALIZADA.canTransitionTo(ProjectStatus.ANALISE_APROVADA));
    }

    @Test
    @DisplayName("Deve permitir transição ANALISE_APROVADA -> INICIADO")
    void shouldAllowTransitionFromAnaliseAprovadaToIniciado() {
        assertTrue(ProjectStatus.ANALISE_APROVADA.canTransitionTo(ProjectStatus.INICIADO));
    }

    @Test
    @DisplayName("Deve permitir transição INICIADO -> PLANEJADO")
    void shouldAllowTransitionFromIniciadoToPlanejado() {
        assertTrue(ProjectStatus.INICIADO.canTransitionTo(ProjectStatus.PLANEJADO));
    }

    @Test
    @DisplayName("Deve permitir transição PLANEJADO -> EM_ANDAMENTO")
    void shouldAllowTransitionFromPlanejadoToEmAndamento() {
        assertTrue(ProjectStatus.PLANEJADO.canTransitionTo(ProjectStatus.EM_ANDAMENTO));
    }

    @Test
    @DisplayName("Deve permitir transição EM_ANDAMENTO -> ENCERRADO")
    void shouldAllowTransitionFromEmAndamentoToEncerrado() {
        assertTrue(ProjectStatus.EM_ANDAMENTO.canTransitionTo(ProjectStatus.ENCERRADO));
    }

    @Test
    @DisplayName("Não deve permitir pular etapas (EM_ANALISE -> INICIADO)")
    void shouldNotAllowSkippingSteps() {
        assertFalse(ProjectStatus.EM_ANALISE.canTransitionTo(ProjectStatus.INICIADO));
    }

    @Test
    @DisplayName("Não deve permitir retroceder (INICIADO -> EM_ANALISE)")
    void shouldNotAllowGoingBackwards() {
        assertFalse(ProjectStatus.INICIADO.canTransitionTo(ProjectStatus.EM_ANALISE));
    }

    @ParameterizedTest
    @EnumSource(value = ProjectStatus.class, names = {"CANCELADO", "ENCERRADO"}, mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("Deve permitir cancelamento de qualquer status ativo")
    void shouldAllowCancellationFromAnyActiveStatus(ProjectStatus status) {
        assertTrue(status.canTransitionTo(ProjectStatus.CANCELADO));
    }

    @Test
    @DisplayName("Não deve permitir cancelar um projeto já cancelado")
    void shouldNotAllowCancellingAlreadyCancelled() {
        assertFalse(ProjectStatus.CANCELADO.canTransitionTo(ProjectStatus.CANCELADO));
    }

    @Test
    @DisplayName("Não deve permitir cancelar um projeto encerrado")
    void shouldNotAllowCancellingEncerrado() {
        assertFalse(ProjectStatus.ENCERRADO.canTransitionTo(ProjectStatus.CANCELADO));
    }

    @Test
    @DisplayName("INICIADO não deve ser deletável")
    void iniciadoShouldNotBeDeletable() {
        assertFalse(ProjectStatus.INICIADO.isDeletable());
    }

    @Test
    @DisplayName("EM_ANDAMENTO não deve ser deletável")
    void emAndamentoShouldNotBeDeletable() {
        assertFalse(ProjectStatus.EM_ANDAMENTO.isDeletable());
    }

    @Test
    @DisplayName("ENCERRADO não deve ser deletável")
    void encerradoShouldNotBeDeletable() {
        assertFalse(ProjectStatus.ENCERRADO.isDeletable());
    }

    @Test
    @DisplayName("EM_ANALISE deve ser deletável")
    void emAnaliseShouldBeDeletable() {
        assertTrue(ProjectStatus.EM_ANALISE.isDeletable());
    }

    @Test
    @DisplayName("CANCELADO deve ser deletável")
    void canceladoShouldBeDeletable() {
        assertTrue(ProjectStatus.CANCELADO.isDeletable());
    }

    @Test
    @DisplayName("EM_ANALISE deve ser considerado ativo")
    void emAnaliseShouldBeActive() {
        assertTrue(ProjectStatus.EM_ANALISE.isActive());
    }

    @Test
    @DisplayName("ENCERRADO não deve ser considerado ativo")
    void encerradoShouldNotBeActive() {
        assertFalse(ProjectStatus.ENCERRADO.isActive());
    }

    @Test
    @DisplayName("CANCELADO não deve ser considerado ativo")
    void canceladoShouldNotBeActive() {
        assertFalse(ProjectStatus.CANCELADO.isActive());
    }
}
