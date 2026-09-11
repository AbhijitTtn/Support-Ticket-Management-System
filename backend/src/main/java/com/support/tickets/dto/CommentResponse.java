package com.support.tickets.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID ticketId,
        String author,
        String body,
        Instant createdAt
) {
}
