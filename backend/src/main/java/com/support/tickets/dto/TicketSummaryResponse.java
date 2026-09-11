package com.support.tickets.dto;

import com.support.tickets.entity.TicketPriority;
import com.support.tickets.entity.TicketStatus;

import java.time.Instant;
import java.util.UUID;

public record TicketSummaryResponse(
        UUID id,
        String title,
        TicketPriority priority,
        TicketStatus status,
        String assignee,
        Instant createdAt,
        Instant updatedAt
) {
}
