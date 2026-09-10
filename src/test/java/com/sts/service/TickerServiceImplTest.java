package com.sts.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import com.sts.binding.*;
import com.sts.repo.TicketRepository;

@ExtendWith(MockitoExtension.class)
class TickerServiceImplTest {
    @Mock private TicketRepository ticketRepository;
    @InjectMocks private TickerServiceImpl service;
    @Test void bookTicketCopiesPassengerDataAndConfirmsTicket(){Passenger p=new Passenger(); p.setName("Test Passenger"); p.setTrainNo("12345"); p.setSource("Delhi"); p.setDest("Mumbai"); p.setDoj("2026-10-01"); when(ticketRepository.save(any(Ticket.class))).thenAnswer(i->{Ticket t=i.getArgument(0); t.setTicketId(1); return t;}); Ticket t=service.bookTicket(p); assertEquals(1,t.getTicketId()); assertEquals("Test Passenger",t.getName()); assertEquals("12345",t.getTrainNo()); assertEquals("Confirmed",t.getTicketStatus());}
    @Test void getTicketReturnsExistingTicket(){Ticket t=new Ticket(); when(ticketRepository.findById(7)).thenReturn(Optional.of(t)); assertEquals(t,service.getTicket(7));}
    @Test void getTicketThrowsWhenMissing(){when(ticketRepository.findById(7)).thenReturn(Optional.empty()); assertThrows(NoSuchElementException.class,()->service.getTicket(7));}
    @Test void cancelTicketChangesStatus(){Ticket t=new Ticket(); t.setTicketStatus("Confirmed"); when(ticketRepository.findById(7)).thenReturn(Optional.of(t)); when(ticketRepository.save(t)).thenReturn(t); assertEquals("Cancelled",service.cancelTicket(7).getTicketStatus());}
    @Test void cancelTicketRejectsAlreadyCancelled(){Ticket t=new Ticket(); t.setTicketStatus("Cancelled"); when(ticketRepository.findById(7)).thenReturn(Optional.of(t)); assertThrows(IllegalStateException.class,()->service.cancelTicket(7)); verify(ticketRepository,never()).save(any());}
    @Test void getAllTicketsReturnsRepositoryResults(){Ticket t=new Ticket(); when(ticketRepository.findAll()).thenReturn(List.of(t)); assertEquals(List.of(t),service.getAllTickets());}
}
