package com.support.tickets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
        @NotBlank(message = "Author is required")
        @Size(min = 1, max = 120, message = "Author must be between 1 and 120 characters")
        String author,

        @NotBlank(message = "Body is required")
        @Size(min = 1, max = 2000, message = "Body must be between 1 and 2000 characters")
        String body
) {
}
