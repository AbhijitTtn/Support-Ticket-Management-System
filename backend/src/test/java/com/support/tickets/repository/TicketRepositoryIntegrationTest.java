package com.support.tickets.repository;

import com.support.tickets.config.JpaConfig;
import com.support.tickets.entity.Ticket;
import com.support.tickets.entity.TicketComment;
import com.support.tickets.entity.TicketPriority;
import com.support.tickets.entity.TicketStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(JpaConfig.class)
@ActiveProfiles("test")
class TicketRepositoryIntegrationTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketCommentRepository ticketCommentRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesAndFindsTicketByUuid() {
        Ticket ticket = newTicket("Repository ticket", "Saved directly", TicketStatus.OPEN);
        Ticket saved = ticketRepository.save(ticket);

        Ticket found = ticketRepository.findById(saved.getId()).orElseThrow();
        assertEquals("Repository ticket", found.getTitle());
        assertTrue(found.getCreatedAt() != null);
        assertTrue(found.getUpdatedAt() != null);
    }

    @Test
    void findAllByFiltersMatchesKeywordInTitleOrDescription() {
        ticketRepository.save(newTicket("Alpha password issue", "Unrelated body", TicketStatus.OPEN));
        ticketRepository.save(newTicket("Beta issue", "Contains PASSWORD token", TicketStatus.OPEN));
        ticketRepository.save(newTicket("Gamma printer", "No match here", TicketStatus.CLOSED));

        List<Ticket> matches = ticketRepository.findAllByFilters(null, "password");

        assertEquals(2, matches.size());
    }

    @Test
    void findAllByFiltersAppliesStatusAndKeywordTogether() {
        ticketRepository.save(newTicket("Alpha password issue", "Body", TicketStatus.OPEN));
        ticketRepository.save(newTicket("Beta password issue", "Body", TicketStatus.IN_PROGRESS));

        List<Ticket> matches = ticketRepository.findAllByFilters(TicketStatus.OPEN, "password");

        assertEquals(1, matches.size());
        assertEquals(TicketStatus.OPEN, matches.getFirst().getStatus());
    }

    @Test
    void findByIdWithCommentsLoadsOrderedComments() {
        Ticket ticket = ticketRepository.save(newTicket("Comment load", "Body", TicketStatus.OPEN));

        TicketComment first = new TicketComment();
        first.setTicket(ticket);
        first.setAuthor("first");
        first.setBody("First");
        ticketCommentRepository.save(first);

        TicketComment second = new TicketComment();
        second.setTicket(ticket);
        second.setAuthor("second");
        second.setBody("Second");
        ticketCommentRepository.save(second);
        entityManager.flush();
        entityManager.clear();

        Ticket loaded = ticketRepository.findByIdWithComments(ticket.getId()).orElseThrow();
        assertEquals(2, loaded.getComments().size());
        assertEquals("First", loaded.getComments().getFirst().getBody());
        assertEquals("Second", loaded.getComments().get(1).getBody());
    }

    private Ticket newTicket(String title, String description, TicketStatus status) {
        Ticket ticket = new Ticket();
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setPriority(TicketPriority.MEDIUM);
        ticket.setStatus(status);
        return ticket;
    }
}
