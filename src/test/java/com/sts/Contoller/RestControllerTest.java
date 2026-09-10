package com.sts.Contoller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import com.sts.binding.*;
import com.sts.service.TicketService;

@ExtendWith(MockitoExtension.class)
class RestControllerTest {
    @Mock private TicketService ticketService;
    @InjectMocks private RestController controller;
    @Test void bookTicketReturnsCreatedTicket() { Passenger p=new Passenger(); Ticket t=new Ticket(); when(ticketService.bookTicket(p)).thenReturn(t); ResponseEntity<Ticket> r=controller.bookTicket(p); assertEquals(HttpStatus.CREATED,r.getStatusCode()); assertEquals(t,r.getBody()); verify(ticketService).bookTicket(p); }
    @Test void getTicketReturnsOkTicket() { Ticket t=new Ticket(); when(ticketService.getTicket(10)).thenReturn(t); ResponseEntity<Ticket> r=controller.getTicket(10); assertEquals(HttpStatus.OK,r.getStatusCode()); assertEquals(t,r.getBody()); verify(ticketService).getTicket(10); }
    @Test void cancelTicketReturnsCancelledTicket() { Ticket t=new Ticket(); t.setTicketStatus("Cancelled"); when(ticketService.cancelTicket(10)).thenReturn(t); ResponseEntity<Ticket> r=controller.cancelTicket(10); assertEquals(HttpStatus.OK,r.getStatusCode()); assertEquals(t,r.getBody()); verify(ticketService).cancelTicket(10); }
    @Test void getAllTicketsReturnsOkList() { List<Ticket> ts=List.of(new Ticket()); when(ticketService.getAllTickets()).thenReturn(ts); ResponseEntity<List<Ticket>> r=controller.getAllTickets(); assertEquals(HttpStatus.OK,r.getStatusCode()); assertEquals(ts,r.getBody()); verify(ticketService).getAllTickets(); }
}
