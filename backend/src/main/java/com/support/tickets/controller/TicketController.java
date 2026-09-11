package com.support.tickets.controller;

import com.support.tickets.dto.CommentResponse;
import com.support.tickets.dto.CreateCommentRequest;
import com.support.tickets.dto.CreateTicketRequest;
import com.support.tickets.dto.TicketDetailResponse;
import com.support.tickets.dto.TicketSummaryResponse;
import com.support.tickets.dto.TransitionStatusRequest;
import com.support.tickets.dto.UpdateTicketRequest;
import com.support.tickets.entity.TicketStatus;
import com.support.tickets.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketDetailResponse create(@Valid @RequestBody CreateTicketRequest request) {
        return ticketService.create(request);
    }

    @GetMapping
    public List<TicketSummaryResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) TicketStatus status
    ) {
        return ticketService.findAll(status, q);
    }

    @GetMapping("/{id}")
    public TicketDetailResponse getById(@PathVariable UUID id) {
        return ticketService.getById(id);
    }

    @PatchMapping("/{id}")
    public TicketDetailResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTicketRequest request
    ) {
        return ticketService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public TicketDetailResponse transitionStatus(
            @PathVariable UUID id,
            @Valid @RequestBody TransitionStatusRequest request
    ) {
        return ticketService.transitionStatus(id, request);
    }

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse addComment(
            @PathVariable UUID id,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        return ticketService.addComment(id, request);
    }
}
