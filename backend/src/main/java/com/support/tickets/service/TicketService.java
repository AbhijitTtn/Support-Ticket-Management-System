package com.support.tickets.service;

import com.support.tickets.dto.CreateCommentRequest;
import com.support.tickets.dto.CreateTicketRequest;
import com.support.tickets.dto.CommentResponse;
import com.support.tickets.dto.TicketDetailResponse;
import com.support.tickets.dto.TicketSummaryResponse;
import com.support.tickets.dto.TransitionStatusRequest;
import com.support.tickets.dto.UpdateTicketRequest;
import com.support.tickets.entity.Ticket;
import com.support.tickets.entity.TicketComment;
import com.support.tickets.entity.TicketStatus;
import com.support.tickets.exception.ResourceNotFoundException;
import com.support.tickets.repository.TicketCommentRepository;
import com.support.tickets.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketCommentRepository ticketCommentRepository;
    private final TicketMapper ticketMapper;
    private final StatusTransitionValidator statusTransitionValidator;

    public TicketService(
            TicketRepository ticketRepository,
            TicketCommentRepository ticketCommentRepository,
            TicketMapper ticketMapper,
            StatusTransitionValidator statusTransitionValidator
    ) {
        this.ticketRepository = ticketRepository;
        this.ticketCommentRepository = ticketCommentRepository;
        this.ticketMapper = ticketMapper;
        this.statusTransitionValidator = statusTransitionValidator;
    }

    @Transactional
    public TicketDetailResponse create(CreateTicketRequest request) {
        Ticket ticket = new Ticket();
        ticket.setTitle(trimRequired(request.title()));
        ticket.setDescription(trimRequired(request.description()));
        ticket.setPriority(request.priority());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setAssignee(trimToNull(request.assignee()));

        Ticket saved = ticketRepository.save(ticket);
        return ticketMapper.toDetail(saved);
    }

    public List<TicketSummaryResponse> findAll(TicketStatus status, String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        return ticketRepository.findAllByFilters(status, normalizedKeyword).stream()
                .map(ticketMapper::toSummary)
                .toList();
    }

    public TicketDetailResponse getById(UUID id) {
        Ticket ticket = findTicketWithComments(id);
        return ticketMapper.toDetail(ticket);
    }

    @Transactional
    public TicketDetailResponse update(UUID id, UpdateTicketRequest request) {
        Ticket ticket = findTicketWithComments(id);
        ticket.setTitle(trimRequired(request.title()));
        ticket.setDescription(trimRequired(request.description()));
        ticket.setPriority(request.priority());
        ticket.setAssignee(trimToNull(request.assignee()));

        Ticket saved = ticketRepository.save(ticket);
        return ticketMapper.toDetail(saved);
    }

    @Transactional
    public TicketDetailResponse transitionStatus(UUID id, TransitionStatusRequest request) {
        Ticket ticket = findTicketWithComments(id);
        statusTransitionValidator.validate(ticket.getStatus(), request.status());
        ticket.setStatus(request.status());

        Ticket saved = ticketRepository.save(ticket);
        return ticketMapper.toDetail(saved);
    }

    @Transactional
    public CommentResponse addComment(UUID ticketId, CreateCommentRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        TicketComment comment = new TicketComment();
        comment.setTicket(ticket);
        comment.setAuthor(trimRequired(request.author()));
        comment.setBody(trimRequired(request.body()));

        TicketComment saved = ticketCommentRepository.save(comment);
        return ticketMapper.toComment(saved);
    }

    private Ticket findTicketWithComments(UUID id) {
        return ticketRepository.findByIdWithComments(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + id));
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String trimRequired(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
