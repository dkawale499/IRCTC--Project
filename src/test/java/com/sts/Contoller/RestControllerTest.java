package com.sts.Contoller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import com.sts.binding.*;
import com.sts.service.TicketService;

@ExtendWith(MockitoExtension.class)
class RestControllerTest {
    @Mock private TicketService ticketService;
    @InjectMocks private RestController controller;
    @Test void bookTicketReturnsCreatedTicket() { Passenger p=new Passenger(); Ticket t=new Ticket(); when(ticketService.bookTicket(p,"user@example.com")).thenReturn(t); ResponseEntity<Ticket> r=controller.bookTicket(p,"user@example.com"); assertEquals(HttpStatus.CREATED,r.getStatusCode()); assertEquals(t,r.getBody()); verify(ticketService).bookTicket(p,"user@example.com"); }
    @Test void getTicketReturnsOkTicket() { Ticket t=new Ticket(); when(ticketService.getTicket(10)).thenReturn(t); ResponseEntity<Ticket> r=controller.getTicket(10); assertEquals(HttpStatus.OK,r.getStatusCode()); assertEquals(t,r.getBody()); verify(ticketService).getTicket(10); }
    @Test void cancelTicketReturnsCancelledTicket() { Ticket t=new Ticket(); t.setTicketStatus("Cancelled"); var auth=new UsernamePasswordAuthenticationToken("user@example.com",null,List.of(new SimpleGrantedAuthority("ROLE_USER"))); when(ticketService.cancelTicket(10,"user@example.com",false)).thenReturn(t); ResponseEntity<Ticket> r=controller.cancelTicket(10,"user@example.com",auth); assertEquals(HttpStatus.OK,r.getStatusCode()); assertEquals(t,r.getBody()); verify(ticketService).cancelTicket(10,"user@example.com",false); }
    @Test void getAllTicketsReturnsOkList() { List<Ticket> ts=List.of(new Ticket()); when(ticketService.getAllTickets()).thenReturn(ts); ResponseEntity<List<Ticket>> r=controller.getAllTickets(null); assertEquals(HttpStatus.OK,r.getStatusCode()); assertEquals(ts,r.getBody()); verify(ticketService).getAllTickets(); }
    @Test void getPassengerTicketsReturnsOnlyAuthenticatedPassengerTickets() { List<Ticket> ts=List.of(new Ticket()); when(ticketService.getPassengerTickets("user@example.com")).thenReturn(ts); ResponseEntity<List<Ticket>> r=controller.getPassengerTickets("user@example.com"); assertEquals(HttpStatus.OK,r.getStatusCode()); assertEquals(ts,r.getBody()); verify(ticketService).getPassengerTickets("user@example.com"); }
    @Test void getAllTicketsCanFilterByStatus() { List<Ticket> ts=List.of(new Ticket()); when(ticketService.getAllTickets(com.sts.binding.TicketStatus.RAC)).thenReturn(ts); ResponseEntity<List<Ticket>> r=controller.getAllTickets("RAC"); assertEquals(HttpStatus.OK,r.getStatusCode()); assertEquals(ts,r.getBody()); verify(ticketService).getAllTickets(com.sts.binding.TicketStatus.RAC); }
    @Test void getAllTicketsRejectsUnknownStatus() { ResponseEntity<List<Ticket>> r=controller.getAllTickets("Unknown"); assertEquals(HttpStatus.BAD_REQUEST,r.getStatusCode()); verifyNoInteractions(ticketService); }
    @Test void getTicketStatusSummaryReturnsSummary() { com.sts.binding.TicketStatusSummary summary = new com.sts.binding.TicketStatusSummary(6, 2, 1, 2, 1); when(ticketService.getTicketStatusSummary()).thenReturn(summary); ResponseEntity<com.sts.binding.TicketStatusSummary> r=controller.getTicketStatusSummary(); assertEquals(HttpStatus.OK,r.getStatusCode()); assertEquals(summary,r.getBody()); }
}
