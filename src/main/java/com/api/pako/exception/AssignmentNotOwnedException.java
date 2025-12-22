package com.api.pako.exception;

import lombok.Getter;

/**
 * Exception thrown when assignment does not belong to the courier
 */
@Getter
public class AssignmentNotOwnedException extends RuntimeException {

    private final Long assignmentId;
    private final Long courierId;

    public AssignmentNotOwnedException(Long assignmentId, Long courierId) {
        super(String.format("Assignment %d does not belong to courier %d", assignmentId, courierId));
        this.assignmentId = assignmentId;
        this.courierId = courierId;
    }

}

