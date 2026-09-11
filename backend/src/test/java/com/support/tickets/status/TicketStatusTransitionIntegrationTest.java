package com.support.tickets.status;

import com.support.tickets.entity.TicketStatus;
import com.support.tickets.support.AbstractApiIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TicketStatusTransitionIntegrationTest extends AbstractApiIntegrationTest {

    @Test
    void allowsFullHappyPathLifecycle() throws Exception {
        UUID ticketId = createOpenTicket("Lifecycle ticket", "Track full lifecycle");

        transitionStatus(ticketId, TicketStatus.IN_PROGRESS);
        transitionStatus(ticketId, TicketStatus.RESOLVED);
        transitionStatus(ticketId, TicketStatus.CLOSED);

        mockMvc.perform(get("/api/tickets/{id}", ticketId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CLOSED")));
    }

    @Test
    void allowsOpenToInProgress() throws Exception {
        UUID ticketId = createOpenTicket("Open ticket", "Ready for work");

        transitionStatus(ticketId, TicketStatus.IN_PROGRESS);

        assertEquals(TicketStatus.IN_PROGRESS, ticketRepository.findById(ticketId).orElseThrow().getStatus());
    }

    @Test
    void allowsOpenToCancelled() throws Exception {
        UUID ticketId = createOpenTicket("Open ticket", "Will be cancelled");

        transitionStatus(ticketId, TicketStatus.CANCELLED);

        assertEquals(TicketStatus.CANCELLED, ticketRepository.findById(ticketId).orElseThrow().getStatus());
    }

    @Test
    void allowsInProgressToResolved() throws Exception {
        UUID ticketId = createOpenTicket("Work ticket", "In progress soon");
        transitionStatus(ticketId, TicketStatus.IN_PROGRESS);

        transitionStatus(ticketId, TicketStatus.RESOLVED);

        assertEquals(TicketStatus.RESOLVED, ticketRepository.findById(ticketId).orElseThrow().getStatus());
    }

    @Test
    void allowsInProgressToCancelled() throws Exception {
        UUID ticketId = createOpenTicket("Work ticket", "Will cancel mid-flight");
        transitionStatus(ticketId, TicketStatus.IN_PROGRESS);

        transitionStatus(ticketId, TicketStatus.CANCELLED);

        assertEquals(TicketStatus.CANCELLED, ticketRepository.findById(ticketId).orElseThrow().getStatus());
    }

    @Test
    void allowsResolvedToClosed() throws Exception {
        UUID ticketId = createOpenTicket("Resolve ticket", "Close after resolve");
        transitionStatus(ticketId, TicketStatus.IN_PROGRESS);
        transitionStatus(ticketId, TicketStatus.RESOLVED);

        transitionStatus(ticketId, TicketStatus.CLOSED);

        assertEquals(TicketStatus.CLOSED, ticketRepository.findById(ticketId).orElseThrow().getStatus());
    }

    @Test
    void rejectsOpenToResolvedWith409AndLeavesStatusUnchanged() throws Exception {
        UUID ticketId = createOpenTicket("Invalid skip", "Cannot skip to resolved");

        mockMvc.perform(patch("/api/tickets/{id}/status", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"RESOLVED\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", is("Invalid status transition from OPEN to RESOLVED")))
                .andExpect(jsonPath("$.fieldErrors", empty()))
                .andExpect(jsonPath("$.stackTrace").doesNotExist());

        assertEquals(TicketStatus.OPEN, ticketRepository.findById(ticketId).orElseThrow().getStatus());
    }

    @Test
    void rejectsInProgressToOpenWith409() throws Exception {
        UUID ticketId = createOpenTicket("Backward move", "Cannot go back");
        transitionStatus(ticketId, TicketStatus.IN_PROGRESS);

        mockMvc.perform(patch("/api/tickets/{id}/status", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"OPEN\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Invalid status transition from IN_PROGRESS to OPEN")));

        assertEquals(TicketStatus.IN_PROGRESS, ticketRepository.findById(ticketId).orElseThrow().getStatus());
    }

    @Test
    void rejectsResolvedToCancelledWith409() throws Exception {
        UUID ticketId = createOpenTicket("Resolved ticket", "Cannot cancel after resolve");
        transitionStatus(ticketId, TicketStatus.IN_PROGRESS);
        transitionStatus(ticketId, TicketStatus.RESOLVED);

        mockMvc.perform(patch("/api/tickets/{id}/status", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Invalid status transition from RESOLVED to CANCELLED")));

        assertEquals(TicketStatus.RESOLVED, ticketRepository.findById(ticketId).orElseThrow().getStatus());
    }

    @Test
    void rejectsClosedToOpenWith409() throws Exception {
        UUID ticketId = createClosedTicket();

        mockMvc.perform(patch("/api/tickets/{id}/status", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"OPEN\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Invalid status transition from CLOSED to OPEN")));

        assertEquals(TicketStatus.CLOSED, ticketRepository.findById(ticketId).orElseThrow().getStatus());
    }

    @Test
    void rejectsCancelledToOpenWith409() throws Exception {
        UUID ticketId = createOpenTicket("Cancelled ticket", "Terminal cancelled state");
        transitionStatus(ticketId, TicketStatus.CANCELLED);

        mockMvc.perform(patch("/api/tickets/{id}/status", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"OPEN\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Invalid status transition from CANCELLED to OPEN")));

        assertEquals(TicketStatus.CANCELLED, ticketRepository.findById(ticketId).orElseThrow().getStatus());
    }

    @Test
    void rejectsSelfTransitionWith409() throws Exception {
        UUID ticketId = createOpenTicket("Self transition", "No-op should fail");

        mockMvc.perform(patch("/api/tickets/{id}/status", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"OPEN\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Invalid status transition from OPEN to OPEN")));
    }

    @Test
    void updateEndpointDoesNotChangeStatus() throws Exception {
        UUID ticketId = createOpenTicket("Immutable status", "Status changes only via status endpoint");
        transitionStatus(ticketId, TicketStatus.IN_PROGRESS);

        mockMvc.perform(patch("/api/tickets/{id}", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated title",
                                  "description": "Updated description text",
                                  "priority": "HIGH",
                                  "assignee": "agent.one"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated title")))
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));

        assertEquals(TicketStatus.IN_PROGRESS, ticketRepository.findById(ticketId).orElseThrow().getStatus());
    }

    private UUID createClosedTicket() throws Exception {
        UUID ticketId = createOpenTicket("Close me", "Full path to closed");
        transitionStatus(ticketId, TicketStatus.IN_PROGRESS);
        transitionStatus(ticketId, TicketStatus.RESOLVED);
        transitionStatus(ticketId, TicketStatus.CLOSED);
        return ticketId;
    }
}
