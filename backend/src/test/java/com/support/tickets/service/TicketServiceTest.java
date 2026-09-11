package com.support.tickets.service;

import com.support.tickets.dto.CreateCommentRequest;
import com.support.tickets.dto.CreateTicketRequest;
import com.support.tickets.dto.TransitionStatusRequest;
import com.support.tickets.dto.UpdateTicketRequest;
import com.support.tickets.entity.Ticket;
import com.support.tickets.entity.TicketComment;
import com.support.tickets.entity.TicketPriority;
import com.support.tickets.entity.TicketStatus;
import com.support.tickets.exception.InvalidStatusTransitionException;
import com.support.tickets.exception.ResourceNotFoundException;
import com.support.tickets.repository.TicketCommentRepository;
import com.support.tickets.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketCommentRepository ticketCommentRepository;

    @Mock
    private TicketMapper ticketMapper;

    @Mock
    private StatusTransitionValidator statusTransitionValidator;

    @InjectMocks
    private TicketService ticketService;

    @Test
    void createSetsOpenStatusAndTrimsAssignee() {
        CreateTicketRequest request = new CreateTicketRequest(
                "  Valid title  ",
                "  Valid description here  ",
                TicketPriority.HIGH,
                "  jane.smith  "
        );

        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ticketService.create(request);

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(captor.capture());

        Ticket saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(TicketStatus.OPEN);
        assertThat(saved.getTitle()).isEqualTo("Valid title");
        assertThat(saved.getDescription()).isEqualTo("Valid description here");
        assertThat(saved.getAssignee()).isEqualTo("jane.smith");
    }

    @Test
    void createClearsBlankAssignee() {
        CreateTicketRequest request = new CreateTicketRequest(
                "Valid title",
                "Valid description",
                TicketPriority.MEDIUM,
                "   "
        );

        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ticketService.create(request);

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(captor.capture());
        assertThat(captor.getValue().getAssignee()).isNull();
    }

    @Test
    void findAllNormalizesBlankKeyword() {
        when(ticketRepository.findAllByFilters(eq(TicketStatus.OPEN), eq(null))).thenReturn(List.of());

        ticketService.findAll(TicketStatus.OPEN, "   ");

        verify(ticketRepository).findAllByFilters(TicketStatus.OPEN, null);
    }

    @Test
    void getByIdThrowsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(ticketRepository.findByIdWithComments(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateDoesNotChangeStatus() {
        UUID id = UUID.randomUUID();
        Ticket ticket = sampleTicket(id, TicketStatus.IN_PROGRESS);
        when(ticketRepository.findByIdWithComments(id)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        UpdateTicketRequest request = new UpdateTicketRequest(
                "Updated title",
                "Updated description text",
                TicketPriority.CRITICAL,
                null
        );

        ticketService.update(id, request);

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
        assertThat(ticket.getTitle()).isEqualTo("Updated title");
        assertThat(ticket.getAssignee()).isNull();
    }

    @Test
    void transitionStatusValidatesBeforePersisting() {
        UUID id = UUID.randomUUID();
        Ticket ticket = sampleTicket(id, TicketStatus.OPEN);
        when(ticketRepository.findByIdWithComments(id)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        ticketService.transitionStatus(id, new TransitionStatusRequest(TicketStatus.IN_PROGRESS));

        verify(statusTransitionValidator).validate(TicketStatus.OPEN, TicketStatus.IN_PROGRESS);
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
    }

    @Test
    void transitionStatusPropagatesInvalidTransition() {
        UUID id = UUID.randomUUID();
        Ticket ticket = sampleTicket(id, TicketStatus.OPEN);
        when(ticketRepository.findByIdWithComments(id)).thenReturn(Optional.of(ticket));
        org.mockito.Mockito.doThrow(new InvalidStatusTransitionException(TicketStatus.OPEN, TicketStatus.RESOLVED))
                .when(statusTransitionValidator)
                .validate(TicketStatus.OPEN, TicketStatus.RESOLVED);

        assertThatThrownBy(() -> ticketService.transitionStatus(id, new TransitionStatusRequest(TicketStatus.RESOLVED)))
                .isInstanceOf(InvalidStatusTransitionException.class);

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.OPEN);
    }

    @Test
    void addCommentThrowsWhenTicketMissing() {
        UUID id = UUID.randomUUID();
        when(ticketRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.addComment(id, new CreateCommentRequest("author", "body")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void addCommentPersistsCommentForExistingTicket() {
        UUID id = UUID.randomUUID();
        Ticket ticket = sampleTicket(id, TicketStatus.OPEN);
        when(ticketRepository.findById(id)).thenReturn(Optional.of(ticket));
        when(ticketCommentRepository.save(any(TicketComment.class))).thenAnswer(invocation -> {
            TicketComment comment = invocation.getArgument(0);
            comment.setId(UUID.randomUUID());
            return comment;
        });

        ticketService.addComment(id, new CreateCommentRequest("  agent  ", "  hello  "));

        ArgumentCaptor<TicketComment> captor = ArgumentCaptor.forClass(TicketComment.class);
        verify(ticketCommentRepository).save(captor.capture());

        TicketComment saved = captor.getValue();
        assertThat(saved.getTicket()).isEqualTo(ticket);
        assertThat(saved.getAuthor()).isEqualTo("agent");
        assertThat(saved.getBody()).isEqualTo("hello");
    }

    private Ticket sampleTicket(UUID id, TicketStatus status) {
        Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setTitle("Title");
        ticket.setDescription("Description");
        ticket.setPriority(TicketPriority.LOW);
        ticket.setStatus(status);
        return ticket;
    }
}
