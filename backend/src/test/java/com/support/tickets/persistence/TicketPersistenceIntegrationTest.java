package com.support.tickets.persistence;

import com.support.tickets.entity.Ticket;
import com.support.tickets.entity.TicketComment;
import com.support.tickets.entity.TicketPriority;
import com.support.tickets.entity.TicketStatus;
import com.support.tickets.support.AbstractApiIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TicketPersistenceIntegrationTest extends AbstractApiIntegrationTest {

    @Test
    void persistsCreatedTicketInH2() throws Exception {
        UUID ticketId = createOpenTicket("Persisted ticket", "Stored in H2 database");

        Ticket persisted = ticketRepository.findById(ticketId).orElseThrow();
        assertEquals("Persisted ticket", persisted.getTitle());
        assertEquals(TicketStatus.OPEN, persisted.getStatus());
        assertEquals(TicketPriority.MEDIUM, persisted.getPriority());
    }

    @Test
    void persistsTicketUpdatesInH2() throws Exception {
        UUID ticketId = createOpenTicket("Original title", "Original description");

        mockMvc.perform(patch("/api/tickets/{id}", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated title",
                                  "description": "Updated description text",
                                  "priority": "CRITICAL",
                                  "assignee": "agent.two"
                                }
                                """))
                .andExpect(status().isOk());

        Ticket persisted = ticketRepository.findById(ticketId).orElseThrow();
        assertEquals("Updated title", persisted.getTitle());
        assertEquals("Updated description text", persisted.getDescription());
        assertEquals(TicketPriority.CRITICAL, persisted.getPriority());
        assertEquals("agent.two", persisted.getAssignee());
    }

    @Test
    void persistsCommentsLinkedToTicketInH2() throws Exception {
        UUID ticketId = createOpenTicket("Comment persistence", "Verify FK relationship");

        mockMvc.perform(post("/api/tickets/{id}/comments", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "author": "db.agent",
                                  "body": "Persisted through H2."
                                }
                                """))
                .andExpect(status().isCreated());

        Ticket ticketWithComments = ticketRepository.findByIdWithComments(ticketId).orElseThrow();
        assertEquals(1, ticketWithComments.getComments().size());

        TicketComment comment = ticketWithComments.getComments().getFirst();
        assertEquals("db.agent", comment.getAuthor());
        assertEquals(ticketId, comment.getTicket().getId());
        assertTrue(ticketCommentRepository.existsById(comment.getId()));
    }

    @Test
    void persistsStatusTransitionsInH2() throws Exception {
        UUID ticketId = createOpenTicket("Status persistence", "Status survives in DB");

        transitionStatus(ticketId, TicketStatus.IN_PROGRESS);

        assertEquals(TicketStatus.IN_PROGRESS, ticketRepository.findById(ticketId).orElseThrow().getStatus());

        mockMvc.perform(get("/api/tickets/{id}", ticketId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void doesNotPersistRejectedStatusTransition() throws Exception {
        UUID ticketId = createOpenTicket("Rejected transition", "Status must remain OPEN");

        mockMvc.perform(patch("/api/tickets/{id}/status", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CLOSED\"}"))
                .andExpect(status().isConflict());

        Ticket persisted = ticketRepository.findById(ticketId).orElseThrow();
        assertEquals(TicketStatus.OPEN, persisted.getStatus());
        assertFalse(persisted.getStatus() == TicketStatus.CLOSED);
    }
}
