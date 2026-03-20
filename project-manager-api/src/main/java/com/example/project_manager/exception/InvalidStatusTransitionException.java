package com.example.project_manager.exception;

import com.example.project_manager.model.ProjectStatus;

public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(ProjectStatus from, ProjectStatus to) {
        super(String.format("Transição de status inválida: %s → %s", from, to));
    }
}
