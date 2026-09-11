package com.support.tickets.dto;

import com.support.tickets.entity.TicketPriority;
import com.support.tickets.entity.TicketStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TicketDetailResponse(
        UUID id,
        String title,
        String description,
        TicketPriority priority,
        TicketStatus status,
        String assignee,
        Instant createdAt,
        Instant updatedAt,
        List<CommentResponse> comments
) {
}
