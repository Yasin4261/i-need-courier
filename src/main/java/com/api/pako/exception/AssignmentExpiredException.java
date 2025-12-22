package com.api.pako.exception;

/**
 * Exception thrown when assignment has expired/timed out
 */
public class AssignmentExpiredException extends RuntimeException {

    private final Long assignmentId;

    public AssignmentExpiredException(Long assignmentId, String message) {
        super(message);
        this.assignmentId = assignmentId;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }
}

