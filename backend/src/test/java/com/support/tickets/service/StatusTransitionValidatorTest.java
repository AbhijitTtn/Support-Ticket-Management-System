package com.support.tickets.service;

import com.support.tickets.entity.TicketStatus;
import com.support.tickets.exception.InvalidStatusTransitionException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StatusTransitionValidatorTest {

    private final StatusTransitionValidator validator = new StatusTransitionValidator();

    @ParameterizedTest
    @MethodSource("allowedTransitions")
    void allowsConfiguredTransitions(TicketStatus from, TicketStatus to) {
        assertDoesNotThrow(() -> validator.validate(from, to));
    }

    @ParameterizedTest
    @MethodSource("disallowedTransitions")
    void rejectsInvalidTransitions(TicketStatus from, TicketStatus to) {
        InvalidStatusTransitionException exception = assertThrows(
                InvalidStatusTransitionException.class,
                () -> validator.validate(from, to)
        );

        assertEquals(from, exception.getCurrentStatus());
        assertEquals(to, exception.getTargetStatus());
        assertEquals(
                "Invalid status transition from " + from + " to " + to,
                exception.getMessage()
        );
    }

    private static Stream<Arguments> allowedTransitions() {
        return Stream.of(
                Arguments.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS),
                Arguments.of(TicketStatus.OPEN, TicketStatus.CANCELLED),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.CLOSED)
        );
    }

    private static Stream<Arguments> disallowedTransitions() {
        return Stream.of(
                Arguments.of(TicketStatus.OPEN, TicketStatus.RESOLVED),
                Arguments.of(TicketStatus.OPEN, TicketStatus.CLOSED),
                Arguments.of(TicketStatus.OPEN, TicketStatus.OPEN),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.OPEN),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.CLOSED),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.OPEN),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.IN_PROGRESS),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.CANCELLED),
                Arguments.of(TicketStatus.CLOSED, TicketStatus.OPEN),
                Arguments.of(TicketStatus.CLOSED, TicketStatus.IN_PROGRESS),
                Arguments.of(TicketStatus.CANCELLED, TicketStatus.OPEN),
                Arguments.of(TicketStatus.CANCELLED, TicketStatus.IN_PROGRESS)
        );
    }
}
