package com.support.tickets.service;

import com.support.tickets.entity.TicketStatus;
import com.support.tickets.exception.InvalidStatusTransitionException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

@Component
public class StatusTransitionValidator {

    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED_TRANSITIONS = allowedTransitions();

    private static Map<TicketStatus, Set<TicketStatus>> allowedTransitions() {
        Map<TicketStatus, Set<TicketStatus>> transitions = new EnumMap<>(TicketStatus.class);
        transitions.put(TicketStatus.OPEN, Set.of(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED));
        transitions.put(TicketStatus.IN_PROGRESS, Set.of(TicketStatus.RESOLVED, TicketStatus.CANCELLED));
        transitions.put(TicketStatus.RESOLVED, Set.of(TicketStatus.CLOSED));
        transitions.put(TicketStatus.CLOSED, Set.of());
        transitions.put(TicketStatus.CANCELLED, Set.of());
        return transitions;
    }

    public void validate(TicketStatus currentStatus, TicketStatus targetStatus) {
        if (currentStatus == targetStatus) {
            throw new InvalidStatusTransitionException(currentStatus, targetStatus);
        }

        Set<TicketStatus> allowedTargets = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowedTargets.contains(targetStatus)) {
            throw new InvalidStatusTransitionException(currentStatus, targetStatus);
        }
    }
}
