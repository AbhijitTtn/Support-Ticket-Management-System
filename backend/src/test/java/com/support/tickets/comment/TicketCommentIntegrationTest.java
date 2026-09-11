package com.support.tickets.comment;

import com.support.tickets.support.AbstractApiIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TicketCommentIntegrationTest extends AbstractApiIntegrationTest {

    @Test
    void createsCommentAndReturnsItOnTicketDetail() throws Exception {
        UUID ticketId = createOpenTicket("Comment host", "Ticket accepts comments");

        mockMvc.perform(post("/api/tickets/{id}/comments", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "author": "support.agent",
                                  "body": "Investigating the reported issue."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticketId", is(ticketId.toString())))
                .andExpect(jsonPath("$.author", is("support.agent")))
                .andExpect(jsonPath("$.body", is("Investigating the reported issue.")))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists());

        mockMvc.perform(get("/api/tickets/{id}", ticketId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments", hasSize(1)))
                .andExpect(jsonPath("$.comments[0].author", is("support.agent")));
    }

    @Test
    void returnsCommentsOrderedByCreatedAtAscending() throws Exception {
        UUID ticketId = createOpenTicket("Ordering", "Comments should be chronological");

        postComment(ticketId, "first.agent", "First comment");
        postComment(ticketId, "second.agent", "Second comment");

        mockMvc.perform(get("/api/tickets/{id}", ticketId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments[0].body", is("First comment")))
                .andExpect(jsonPath("$.comments[1].body", is("Second comment")));
    }

    @Test
    void returns404WhenCommentingOnMissingTicket() throws Exception {
        UUID missingId = UUID.randomUUID();

        mockMvc.perform(post("/api/tickets/{id}/comments", missingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "author": "support.agent",
                                  "body": "This should not persist."
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Ticket not found: " + missingId)))
                .andExpect(jsonPath("$.fieldErrors", empty()));
    }

    @Test
    void rejectsCommentWithMissingRequiredFields() throws Exception {
        UUID ticketId = createOpenTicket("Validation host", "Comment validation");

        mockMvc.perform(post("/api/tickets/{id}/comments", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("author")))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("body")));
    }

    @Test
    void rejectsCommentBodyAboveMaximumLength() throws Exception {
        UUID ticketId = createOpenTicket("Validation host", "Comment body length");

        mockMvc.perform(post("/api/tickets/{id}/comments", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "author": "support.agent",
                                  "body": "%s"
                                }
                                """.formatted(repeatChar('x', 2001))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("body")));
    }

    private void postComment(UUID ticketId, String author, String body) throws Exception {
        mockMvc.perform(post("/api/tickets/{id}/comments", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "author": "%s",
                                  "body": "%s"
                                }
                                """.formatted(author, body)))
                .andExpect(status().isCreated());
    }
}
