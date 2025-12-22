package com.api.pako.exception;

import lombok.Getter;

/**
 * Exception thrown when assignment has expired/timed out
 */
public class AssignmentExpiredException extends RuntimeException {

    @Getter
    private final Long assignmentId;

    public AssignmentExpiredException(Long assignmentId, String message) {
        super(message);
        this.assignmentId = assignmentId;
    }
}

