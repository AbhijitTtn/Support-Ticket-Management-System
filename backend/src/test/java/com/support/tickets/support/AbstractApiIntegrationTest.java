package com.support.tickets.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.support.tickets.dto.CreateTicketRequest;
import com.support.tickets.entity.TicketPriority;
import com.support.tickets.entity.TicketStatus;
import com.support.tickets.repository.TicketCommentRepository;
import com.support.tickets.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractApiIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected TicketRepository ticketRepository;

    @Autowired
    protected TicketCommentRepository ticketCommentRepository;

    @BeforeEach
    void cleanDatabase() {
        ticketCommentRepository.deleteAll();
        ticketRepository.deleteAll();
    }

    protected UUID createTicket(String title, String description, TicketPriority priority, String assignee)
            throws Exception {
        return createTicket(new CreateTicketRequest(title, description, priority, assignee));
    }

    protected UUID createTicket(CreateTicketRequest request) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return UUID.fromString(readJson(result).get("id").asText());
    }

    protected UUID createOpenTicket(String title, String description) throws Exception {
        return createTicket(title, description, TicketPriority.MEDIUM, null);
    }

    protected void transitionStatus(UUID ticketId, TicketStatus status) throws Exception {
        mockMvc.perform(patch("/api/tickets/{id}/status", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"" + status + "\"}"))
                .andExpect(status().isOk());
    }

    protected JsonNode readJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    protected String repeatChar(char character, int count) {
        return String.valueOf(character).repeat(count);
    }
}
