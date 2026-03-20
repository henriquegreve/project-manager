package com.example.project_manager.model;

import java.util.Map;
import java.util.Set;

public enum ProjectStatus {

    EM_ANALISE,
    ANALISE_REALIZADA,
    ANALISE_APROVADA,
    INICIADO,
    PLANEJADO,
    EM_ANDAMENTO,
    ENCERRADO,
    CANCELADO;

    private static final Map<ProjectStatus, ProjectStatus> NEXT_STATUS = Map.of(
            EM_ANALISE, ANALISE_REALIZADA,
            ANALISE_REALIZADA, ANALISE_APROVADA,
            ANALISE_APROVADA, INICIADO,
            INICIADO, PLANEJADO,
            PLANEJADO, EM_ANDAMENTO,
            EM_ANDAMENTO, ENCERRADO
    );

    private static final Set<ProjectStatus> NON_DELETABLE = Set.of(INICIADO, EM_ANDAMENTO, ENCERRADO);

    public boolean canTransitionTo(ProjectStatus target) {
        if (target == CANCELADO) {
            return this != CANCELADO && this != ENCERRADO;
        }
        return NEXT_STATUS.get(this) == target;
    }

    public boolean isDeletable() {
        return !NON_DELETABLE.contains(this);
    }

    public boolean isActive() {
        return this != ENCERRADO && this != CANCELADO;
    }
}
