package com.support.tickets.validation;

import com.support.tickets.dto.CreateTicketRequest;
import com.support.tickets.entity.TicketPriority;
import com.support.tickets.support.AbstractApiIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TicketValidationIntegrationTest extends AbstractApiIntegrationTest {

    @Test
    void rejectsMissingRequiredFieldsOnCreate() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("title")))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("description")))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("priority")));
    }

    @Test
    void rejectsBlankTitleOnCreate() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTicketRequest(
                                "   ",
                                "Valid description",
                                TicketPriority.LOW,
                                null
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("title")));
    }

    @Test
    void acceptsTitleAtMinimumLength() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTicketRequest(
                                repeatChar('a', 3),
                                repeatChar('b', 5),
                                TicketPriority.LOW,
                                null
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is(repeatChar('a', 3))));
    }

    @Test
    void rejectsTitleBelowMinimumLength() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTicketRequest(
                                repeatChar('a', 2),
                                repeatChar('b', 5),
                                TicketPriority.LOW,
                                null
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("title")));
    }

    @Test
    void acceptsTitleAtMaximumLength() throws Exception {
        String title = repeatChar('a', 120);

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTicketRequest(
                                title,
                                repeatChar('b', 5),
                                TicketPriority.LOW,
                                null
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is(title)));
    }

    @Test
    void rejectsTitleAboveMaximumLength() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTicketRequest(
                                repeatChar('a', 121),
                                repeatChar('b', 5),
                                TicketPriority.LOW,
                                null
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("title")));
    }

    @Test
    void acceptsDescriptionAtMinimumLength() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTicketRequest(
                                "Valid title",
                                repeatChar('b', 5),
                                TicketPriority.LOW,
                                null
                        ))))
                .andExpect(status().isCreated());
    }

    @Test
    void rejectsDescriptionBelowMinimumLength() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTicketRequest(
                                "Valid title",
                                repeatChar('b', 4),
                                TicketPriority.LOW,
                                null
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("description")));
    }

    @Test
    void acceptsDescriptionAtMaximumLength() throws Exception {
        String description = repeatChar('b', 5000);

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTicketRequest(
                                "Valid title",
                                description,
                                TicketPriority.LOW,
                                null
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description", is(description)));
    }

    @Test
    void rejectsDescriptionAboveMaximumLength() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTicketRequest(
                                "Valid title",
                                repeatChar('b', 5001),
                                TicketPriority.LOW,
                                null
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("description")));
    }

    @Test
    void rejectsInvalidPriorityOnCreate() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Valid title",
                                  "description": "Valid description",
                                  "priority": "URGENT"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsOversizedAssigneeOnUpdate() throws Exception {
        UUID ticketId = createOpenTicket("Valid title", "Valid description");

        mockMvc.perform(patch("/api/tickets/{id}", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Valid title",
                                  "description": "Valid description",
                                  "priority": "LOW",
                                  "assignee": "%s"
                                }
                                """.formatted(repeatChar('x', 121))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("assignee")));
    }

    @Test
    void errorResponseDoesNotExposeStackTrace() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.path", is("/api/tickets")))
                .andExpect(jsonPath("$.fieldErrors", hasSize(3)))
                .andExpect(jsonPath("$.stackTrace").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist());
    }
}
