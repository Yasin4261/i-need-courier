package com.api.demo.exception;

import com.api.demo.model.enums.AssignmentStatus;

/**
 * Exception thrown when assignment is in invalid status for the operation
 */
public class InvalidAssignmentStatusException extends RuntimeException {

    private final Long assignmentId;
    private final AssignmentStatus currentStatus;
    private final AssignmentStatus expectedStatus;

    public InvalidAssignmentStatusException(Long assignmentId, AssignmentStatus currentStatus, AssignmentStatus expectedStatus) {
        super(String.format("Assignment %d is in %s status, expected %s", assignmentId, currentStatus, expectedStatus));
        this.assignmentId = assignmentId;
        this.currentStatus = currentStatus;
        this.expectedStatus = expectedStatus;
    }

    public InvalidAssignmentStatusException(Long assignmentId, AssignmentStatus currentStatus, String message) {
        super(message);
        this.assignmentId = assignmentId;
        this.currentStatus = currentStatus;
        this.expectedStatus = null;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public AssignmentStatus getCurrentStatus() {
        return currentStatus;
    }

    public AssignmentStatus getExpectedStatus() {
        return expectedStatus;
    }
}

