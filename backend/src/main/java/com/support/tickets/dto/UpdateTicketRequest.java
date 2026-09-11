package com.support.tickets.dto;

import com.support.tickets.entity.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateTicketRequest(
        @NotBlank(message = "Title is required")
        @Size(min = 3, max = 120, message = "Title must be between 3 and 120 characters")
        String title,

        @NotBlank(message = "Description is required")
        @Size(min = 5, max = 5000, message = "Description must be between 5 and 5000 characters")
        String description,

        @NotNull(message = "Priority is required")
        TicketPriority priority,

        @Size(max = 120, message = "Assignee must be at most 120 characters")
        String assignee
) {
}
