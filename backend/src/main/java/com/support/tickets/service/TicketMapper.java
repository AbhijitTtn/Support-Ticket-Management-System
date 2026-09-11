package com.support.tickets.service;

import com.support.tickets.dto.CommentResponse;
import com.support.tickets.dto.TicketDetailResponse;
import com.support.tickets.dto.TicketSummaryResponse;
import com.support.tickets.entity.Ticket;
import com.support.tickets.entity.TicketComment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TicketMapper {

    public TicketSummaryResponse toSummary(Ticket ticket) {
        return new TicketSummaryResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getAssignee(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }

    public TicketDetailResponse toDetail(Ticket ticket) {
        List<CommentResponse> comments = ticket.getComments().stream()
                .map(this::toComment)
                .toList();

        return new TicketDetailResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getAssignee(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                comments
        );
    }

    public CommentResponse toComment(TicketComment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getTicketId(),
                comment.getAuthor(),
                comment.getBody(),
                comment.getCreatedAt()
        );
    }
}
