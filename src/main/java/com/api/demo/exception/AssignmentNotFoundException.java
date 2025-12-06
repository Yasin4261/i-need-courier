package com.api.demo.exception;

/**
 * Exception thrown when assignment is not found
 */
public class AssignmentNotFoundException extends RuntimeException {

    private final Long assignmentId;

    public AssignmentNotFoundException(Long assignmentId) {
        super(String.format("Assignment with id %d not found", assignmentId));
        this.assignmentId = assignmentId;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }
}

