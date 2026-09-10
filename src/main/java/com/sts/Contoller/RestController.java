package com.sts.Contoller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.access.prepost.PreAuthorize;

import com.sts.binding.Passenger;
import com.sts.binding.Ticket;
import com.sts.service.TicketService;

@org.springframework.web.bind.annotation.RestController
public class RestController {
    @Autowired
    private TicketService service;

    @PostMapping(value = "/ticket", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Ticket> bookTicket(@RequestBody Passenger passenger) {
        Ticket ticket = service.bookTicket(passenger);
        return new ResponseEntity<>(ticket, HttpStatus.CREATED);
    }

    @GetMapping(value = "ticket/{tid}", produces = "application/json")
    public ResponseEntity<Ticket> getTicket(@PathVariable Integer tid) {
        Ticket ticket = service.getTicket(tid);
        return new ResponseEntity<>(ticket, HttpStatus.OK);
    }

    @PostMapping(value = "/ticket/{tid}/cancel", produces = "application/json")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Ticket> cancelTicket(@PathVariable Integer tid) {
        return new ResponseEntity<>(service.cancelTicket(tid), HttpStatus.OK);
    }

    @GetMapping(value = "/tickets", produces = "application/json")
    public ResponseEntity<List<Ticket>> getAllTickets() {
        return new ResponseEntity<>(service.getAllTickets(), HttpStatus.OK);
    }
}
