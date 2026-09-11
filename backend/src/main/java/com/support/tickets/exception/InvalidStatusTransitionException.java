package com.support.tickets.exception;

import com.support.tickets.entity.TicketStatus;

public class InvalidStatusTransitionException extends RuntimeException {

    private final TicketStatus currentStatus;
    private final TicketStatus targetStatus;

    public InvalidStatusTransitionException(TicketStatus currentStatus, TicketStatus targetStatus) {
        super("Invalid status transition from " + currentStatus + " to " + targetStatus);
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }

    public TicketStatus getCurrentStatus() {
        return currentStatus;
    }

    public TicketStatus getTargetStatus() {
        return targetStatus;
    }
}
