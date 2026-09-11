package com.support.tickets.repository;

import com.support.tickets.entity.Ticket;
import com.support.tickets.entity.TicketStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    @Query("""
            SELECT t FROM Ticket t
            WHERE (:status IS NULL OR t.status = :status)
              AND (
                    :keyword IS NULL
                    OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  )
            ORDER BY t.updatedAt DESC
            """)
    List<Ticket> findAllByFilters(@Param("status") TicketStatus status, @Param("keyword") String keyword);

    @EntityGraph(attributePaths = "comments")
    @Query("SELECT t FROM Ticket t WHERE t.id = :id")
    Optional<Ticket> findByIdWithComments(@Param("id") UUID id);
}
