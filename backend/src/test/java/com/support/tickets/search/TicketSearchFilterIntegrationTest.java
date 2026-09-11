package com.support.tickets.search;

import com.support.tickets.entity.TicketPriority;
import com.support.tickets.entity.TicketStatus;
import com.support.tickets.support.AbstractApiIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TicketSearchFilterIntegrationTest extends AbstractApiIntegrationTest {

    private UUID openPasswordTicketId;
    private UUID inProgressTicketId;
    private UUID unrelatedTicketId;

    @BeforeEach
    void seedTickets() throws Exception {
        openPasswordTicketId = createTicket(
                "Password reset failure",
                "User cannot login to billing portal",
                TicketPriority.HIGH,
                null
        );

        inProgressTicketId = createTicket(
                "Login page styling",
                "Password field misaligned on mobile",
                TicketPriority.LOW,
                null
        );
        transitionStatus(inProgressTicketId, TicketStatus.IN_PROGRESS);

        unrelatedTicketId = createTicket(
                "Printer offline",
                "Office printer needs toner",
                TicketPriority.MEDIUM,
                null
        );
    }

    @Test
    void searchesByTitleKeywordCaseInsensitively() throws Exception {
        mockMvc.perform(get("/api/tickets").param("q", "PASSWORD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void searchesByDescriptionKeywordOnly() throws Exception {
        mockMvc.perform(get("/api/tickets").param("q", "toner"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(unrelatedTicketId.toString())));
    }

    @Test
    void returnsEmptyListWhenSearchDoesNotMatch() throws Exception {
        mockMvc.perform(get("/api/tickets").param("q", "nonexistent-keyword"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void treatsBlankSearchAsNoTextFilter() throws Exception {
        mockMvc.perform(get("/api/tickets").param("q", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void filtersByStatus() throws Exception {
        mockMvc.perform(get("/api/tickets").param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.status == 'OPEN')]", hasSize(2)));
    }

    @Test
    void combinesSearchAndStatusWithAndSemantics() throws Exception {
        mockMvc.perform(get("/api/tickets")
                        .param("q", "password")
                        .param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(openPasswordTicketId.toString())))
                .andExpect(jsonPath("$[0].status", is("OPEN")));
    }

    @Test
    void returns400ForInvalidStatusFilter() throws Exception {
        mockMvc.perform(get("/api/tickets").param("status", "NOT_A_STATUS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }
}
