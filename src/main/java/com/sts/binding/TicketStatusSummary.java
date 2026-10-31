package com.sts.binding;

import java.util.List;

public record TicketStatusSummary(
        long totalTickets,
        long confirmed,
        long rac,
        long waitlisted,
        long cancelled) {

    public static TicketStatusSummary from(List<Ticket> tickets) {
        return new TicketStatusSummary(
                tickets.size(),
                count(tickets, TicketStatus.CONFIRMED),
                count(tickets, TicketStatus.RAC),
                count(tickets, TicketStatus.WAITLISTED),
                count(tickets, TicketStatus.CANCELLED));
    }

    private static long count(List<Ticket> tickets, TicketStatus status) {
        return tickets.stream()
                .filter(ticket -> status.matches(ticket.getTicketStatus()))
                .count();
    }
}
