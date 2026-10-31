package com.sts.Contoller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.sts.binding.Passenger;
import com.sts.binding.Ticket;
import com.sts.binding.TicketStatus;
import com.sts.binding.TicketStatusSummary;
import com.sts.service.TicketService;

@org.springframework.web.bind.annotation.RestController
public class RestController {
    @Autowired
    private TicketService service;

    @PostMapping(value = "/ticket", consumes = "application/json", produces = "application/json")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Ticket> bookTicket(@RequestBody Passenger passenger,
            @AuthenticationPrincipal String email) {
        Ticket ticket = service.bookTicket(passenger, email);
        return new ResponseEntity<>(ticket, HttpStatus.CREATED);
    }

    @GetMapping(value = {"/ticket/{tid}", "/tickets/pnr/{tid}"}, produces = "application/json")
    public ResponseEntity<Ticket> getTicket(@PathVariable Integer tid) {
        Ticket ticket = service.getTicket(tid);
        return new ResponseEntity<>(ticket, HttpStatus.OK);
    }

    @PostMapping(value = "/ticket/{tid}/cancel", produces = "application/json")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Ticket> cancelTicket(@PathVariable Integer tid,
            @AuthenticationPrincipal String email, Authentication authentication) {
        boolean administrator = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        return new ResponseEntity<>(service.cancelTicket(tid, email, administrator), HttpStatus.OK);
    }

    @GetMapping(value = "/tickets", produces = "application/json")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Ticket>> getAllTickets(@RequestParam(required = false) String status) {
        if (status == null) {
            return ResponseEntity.ok(service.getAllTickets());
        }
        return TicketStatus.from(status)
                .map(ticketStatus -> ResponseEntity.ok(service.getAllTickets(ticketStatus)))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping(value = "/tickets/mine", produces = "application/json")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Ticket>> getPassengerTickets(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(service.getPassengerTickets(email));
    }

    @GetMapping(value = "/tickets/summary", produces = "application/json")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketStatusSummary> getTicketStatusSummary() {
        return ResponseEntity.ok(service.getTicketStatusSummary());
    }
}
