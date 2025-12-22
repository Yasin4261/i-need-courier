package com.api.pako.exception;

/**
 * Exception thrown when assignment does not belong to the courier
 */
public class AssignmentNotOwnedException extends RuntimeException {

    private final Long assignmentId;
    private final Long courierId;

    public AssignmentNotOwnedException(Long assignmentId, Long courierId) {
        super(String.format("Assignment %d does not belong to courier %d", assignmentId, courierId));
        this.assignmentId = assignmentId;
        this.courierId = courierId;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public Long getCourierId() {
        return courierId;
    }
}

