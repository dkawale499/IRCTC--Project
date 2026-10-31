package com.sts.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import com.sts.binding.*;
import com.sts.login.Login;
import com.sts.registration.Registration;
import com.sts.repo.TicketRepository;
import com.sts.repo.TrainScheduleRepository;
import com.sts.train.TrainSchedule;

@ExtendWith(MockitoExtension.class)
class TickerServiceImplTest {
    @Mock private TicketRepository ticketRepository;
    @Mock private TrainScheduleRepository trainScheduleRepository;
    @Mock private WalletService walletService;
    @InjectMocks private TickerServiceImpl service;
    @Test void bookTicketCopiesPassengerDataAndConfirmsTicket(){Passenger p=validPassenger(); Registration owner=new Registration(); TrainSchedule schedule=scheduledTrain("100.00"); when(trainScheduleRepository.findByTrainNoIgnoreCaseAndJourneyDate("12345",LocalDate.parse(p.getDoj()))).thenReturn(Optional.of(schedule)); when(walletService.lockAndValidateTicketPayment("user@example.com",new BigDecimal("100.00"))).thenReturn(owner); when(ticketRepository.save(any(Ticket.class))).thenAnswer(i->{Ticket t=i.getArgument(0); t.setTicketId(1); return t;}); Ticket t=service.bookTicket(p,"user@example.com"); assertEquals(1,t.getTicketId()); assertEquals("Test Passenger",t.getName()); assertEquals("12345",t.getTrainNo()); assertEquals("Confirmed",t.getTicketStatus()); assertEquals(new BigDecimal("100.00"),t.getTicketFare()); verify(walletService).debitTicketFare(owner,t);}
    @Test void authenticatedBookingChargesWalletAndLinksTicketToPassenger() {
        Passenger passenger = validPassenger();
        Registration owner = new Registration();
        TrainSchedule schedule=scheduledTrain("275.50");
        when(trainScheduleRepository.findByTrainNoIgnoreCaseAndJourneyDate("12345",LocalDate.parse(passenger.getDoj()))).thenReturn(Optional.of(schedule));
        when(walletService.lockAndValidateTicketPayment("user@example.com",new BigDecimal("275.50"))).thenReturn(owner);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket ticket = invocation.getArgument(0);
            ticket.setTicketId(21);
            return ticket;
        });
        Ticket ticket = service.bookTicket(passenger, "user@example.com");
        assertEquals(owner, ticket.getRegistration());
        assertEquals(new BigDecimal("275.50"), ticket.getTicketFare());
        verify(walletService).debitTicketFare(owner, ticket);
    }
    @Test void bookingRejectsPassengerRouteThatDoesNotMatchScheduledTrain() {
        Passenger passenger=validPassenger();
        passenger.setDest("Jaipur");
        when(trainScheduleRepository.findByTrainNoIgnoreCaseAndJourneyDate("12345",LocalDate.parse(passenger.getDoj())))
                .thenReturn(Optional.of(scheduledTrain("100.00")));
        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.bookTicket(passenger,"user@example.com"));
        verifyNoInteractions(walletService);
        verify(ticketRepository,never()).save(any());
    }
    @Test void getTicketReturnsExistingTicket(){Ticket t=new Ticket(); when(ticketRepository.findById(7)).thenReturn(Optional.of(t)); assertEquals(t,service.getTicket(7));}
    @Test void getTicketThrowsWhenMissing(){when(ticketRepository.findById(7)).thenReturn(Optional.empty()); assertThrows(NoSuchElementException.class,()->service.getTicket(7));}
    @Test void cancelTicketChangesStatus(){Ticket t=new Ticket(); t.setTicketStatus("Confirmed"); when(ticketRepository.findById(7)).thenReturn(Optional.of(t)); when(ticketRepository.save(t)).thenReturn(t); assertEquals("Cancelled",service.cancelTicket(7,"admin@example.com",true).getTicketStatus());}
    @Test void cancelTicketRejectsAlreadyCancelled(){Ticket t=new Ticket(); t.setTicketStatus("Cancelled"); when(ticketRepository.findById(7)).thenReturn(Optional.of(t)); assertThrows(IllegalStateException.class,()->service.cancelTicket(7,"admin@example.com",true)); verify(ticketRepository,never()).save(any());}
    @Test void passengerCancellationRefundsWalletAfterTicketCancellation() {
        Registration owner = new Registration();
        owner.setWalletBalance(new java.math.BigDecimal("0.00"));
        Login login = new Login();
        login.setEmail("user@example.com");
        owner.setLogin(login);
        Ticket ticket = new Ticket();
        ticket.setTicketStatus("Confirmed");
        ticket.setRegistration(owner);
        when(ticketRepository.findById(7)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        assertEquals("Cancelled", service.cancelTicket(7, "user@example.com", false).getTicketStatus());
        verify(walletService).refundTicketFare(owner, ticket);
    }
    @Test void passengerCannotCancelAnotherUsersTicket() {
        Registration owner = new Registration();
        Login login = new Login();
        login.setEmail("other@example.com");
        owner.setLogin(login);
        Ticket ticket = new Ticket();
        ticket.setTicketStatus("Confirmed");
        ticket.setRegistration(owner);
        when(ticketRepository.findById(7)).thenReturn(Optional.of(ticket));
        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.cancelTicket(7, "user@example.com", false));
        verify(ticketRepository, never()).save(any());
    }
    @Test void getAllTicketsReturnsRepositoryResults(){Ticket t=new Ticket(); when(ticketRepository.findAll()).thenReturn(List.of(t)); assertEquals(List.of(t),service.getAllTickets());}
    @Test void getPassengerTicketsQueriesByAuthenticatedAccountEmail(){Ticket t=new Ticket(); when(ticketRepository.findAllByRegistration_Login_EmailIgnoreCase("user@example.com")).thenReturn(List.of(t)); assertEquals(List.of(t),service.getPassengerTickets("user@example.com")); verify(ticketRepository).findAllByRegistration_Login_EmailIgnoreCase("user@example.com");}
    @Test void getAllTicketsFiltersByStatusIgnoringCase(){Ticket confirmed=new Ticket(); confirmed.setTicketStatus("confirmed"); Ticket rac=new Ticket(); rac.setTicketStatus("RAC"); when(ticketRepository.findAll()).thenReturn(List.of(confirmed,rac)); assertEquals(List.of(rac),service.getAllTickets(TicketStatus.RAC));}
    @Test void statusSummaryCountsAllSupportedStatuses(){Ticket confirmed=new Ticket(); confirmed.setTicketStatus("Confirmed"); Ticket rac=new Ticket(); rac.setTicketStatus("RAC"); Ticket waitlisted=new Ticket(); waitlisted.setTicketStatus("Waitlisted"); Ticket cancelled=new Ticket(); cancelled.setTicketStatus("Cancelled"); Ticket unknown=new Ticket(); unknown.setTicketStatus("Other"); when(ticketRepository.findAll()).thenReturn(List.of(confirmed,rac,waitlisted,cancelled,unknown)); assertEquals(new TicketStatusSummary(5,1,1,1,1),service.getTicketStatusSummary());}

    private Passenger validPassenger() {
        Passenger passenger=new Passenger();
        passenger.setName("Test Passenger");
        passenger.setTrainNo("12345");
        passenger.setSource("Delhi");
        passenger.setDest("Mumbai");
        passenger.setDoj("2026-10-01");
        return passenger;
    }

    private TrainSchedule scheduledTrain(String fare) {
        TrainSchedule schedule=new TrainSchedule();
        schedule.setTrainNo("12345");
        schedule.setSource("Delhi");
        schedule.setDestination("Mumbai");
        schedule.setTicketFare(new BigDecimal(fare));
        return schedule;
    }
}
