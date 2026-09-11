package com.support.tickets.dto;

import com.support.tickets.entity.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record TransitionStatusRequest(
        @NotNull(message = "Status is required")
        TicketStatus status
) {
}
