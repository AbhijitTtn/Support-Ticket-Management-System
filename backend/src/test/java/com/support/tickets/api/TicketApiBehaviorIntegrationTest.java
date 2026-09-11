package com.support.tickets.api;

import com.support.tickets.dto.CreateTicketRequest;
import com.support.tickets.entity.TicketPriority;
import com.support.tickets.support.AbstractApiIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TicketApiBehaviorIntegrationTest extends AbstractApiIntegrationTest {

    @Test
    void createsTicketWithOpenStatusAndOptionalAssignee() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTicketRequest(
                                "Cannot reset password",
                                "User reports password reset email never arrives.",
                                TicketPriority.HIGH,
                                "jane.smith"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status", is("OPEN")))
                .andExpect(jsonPath("$.priority", is("HIGH")))
                .andExpect(jsonPath("$.assignee", is("jane.smith")))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                .andExpect(jsonPath("$.comments", empty()));
    }

    @Test
    void updatesEditableFieldsWithoutChangingStatus() throws Exception {
        UUID ticketId = createOpenTicket("Original title", "Original description");

        mockMvc.perform(patch("/api/tickets/{id}", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated title",
                                  "description": "Updated description for ticket",
                                  "priority": "CRITICAL",
                                  "assignee": "john.doe"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated title")))
                .andExpect(jsonPath("$.description", is("Updated description for ticket")))
                .andExpect(jsonPath("$.priority", is("CRITICAL")))
                .andExpect(jsonPath("$.assignee", is("john.doe")))
                .andExpect(jsonPath("$.status", is("OPEN")));
    }

    @Test
    void returns404WhenGettingMissingTicket() throws Exception {
        UUID missingId = UUID.randomUUID();

        mockMvc.perform(get("/api/tickets/{id}", missingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Ticket not found: " + missingId)));
    }

    @Test
    void returns404WhenUpdatingMissingTicket() throws Exception {
        UUID missingId = UUID.randomUUID();

        mockMvc.perform(patch("/api/tickets/{id}", missingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated title",
                                  "description": "Updated description",
                                  "priority": "LOW"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Ticket not found: " + missingId)));
    }

    @Test
    void returns404WhenTransitioningMissingTicket() throws Exception {
        UUID missingId = UUID.randomUUID();

        mockMvc.perform(patch("/api/tickets/{id}/status", missingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Ticket not found: " + missingId)));
    }
}
