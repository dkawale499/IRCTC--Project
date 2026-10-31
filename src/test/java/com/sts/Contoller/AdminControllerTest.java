package com.sts.Contoller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.sts.binding.Ticket;
import com.sts.registration.RegisteredUser;
import com.sts.registration.RegistrationRequest;
import com.sts.service.*;
import com.sts.train.CoachConfigurationRequest;
import com.sts.train.TrainSchedule;
import com.sts.train.TrainScheduleRequest;
import com.sts.train.TicketFareRequest;
import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {
    @Mock private TicketService ticketService;
    @Mock private TrainScheduleService trainScheduleService;
    @Mock private RegistrationService registrationService;
    @InjectMocks private AdminController controller;
    @Test void getRegisteredUsersReturnsSafeUserProfiles() {
        List<RegisteredUser> users = List.of(new RegisteredUser(1L, "Test User", "user@example.com", "1234567890"));
        when(registrationService.getRegisteredUsers()).thenReturn(users);
        ResponseEntity<List<RegisteredUser>> response = controller.getRegisteredUsers();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(users, response.getBody());
        verify(registrationService).getRegisteredUsers();
    }
    @Test void createRegisteredUserReturnsCreatedProfile() {
        RegistrationRequest request = new RegistrationRequest("Test User", "user@example.com", "1234567890", "password123");
        RegisteredUser user = new RegisteredUser(1L, "Test User", "user@example.com", "1234567890");
        when(registrationService.createRegisteredUser(request)).thenReturn(user);
        ResponseEntity<RegisteredUser> response = controller.createRegisteredUser(request);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(user, response.getBody());
        verify(registrationService).createRegisteredUser(request);
    }
    @Test void deleteRegisteredUserReturnsNoContent() {
        ResponseEntity<Void> response = controller.deleteRegisteredUser(1L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(registrationService).deleteRegisteredUser(1L);
    }
    @Test void getAllTicketsReturnsTicketsForAdminRoute() {
        List<Ticket> tickets = List.of(new Ticket()); when(ticketService.getAllTickets()).thenReturn(tickets);
        ResponseEntity<List<Ticket>> response = controller.getAllTickets(null);
        assertEquals(HttpStatus.OK, response.getStatusCode()); assertEquals(tickets, response.getBody()); verify(ticketService).getAllTickets();
    }
    @Test void getTicketsCanFilterByStatus() {
        List<Ticket> tickets = List.of(new Ticket());
        when(ticketService.getAllTickets(com.sts.binding.TicketStatus.CONFIRMED)).thenReturn(tickets);
        ResponseEntity<List<Ticket>> response = controller.getAllTickets("Confirmed");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tickets, response.getBody());
        verify(ticketService).getAllTickets(com.sts.binding.TicketStatus.CONFIRMED);
    }
    @Test void getTicketStatusSummaryReturnsSummary() {
        com.sts.binding.TicketStatusSummary summary = new com.sts.binding.TicketStatusSummary(6, 2, 1, 2, 1);
        when(ticketService.getTicketStatusSummary()).thenReturn(summary);
        ResponseEntity<com.sts.binding.TicketStatusSummary> response = controller.getTicketStatusSummary();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(summary, response.getBody());
    }
    @Test void getAllTrainsReturnsSchedules() {
        List<TrainSchedule> schedules = List.of(new TrainSchedule()); when(trainScheduleService.getAllSchedules()).thenReturn(schedules);
        ResponseEntity<List<TrainSchedule>> response = controller.getAllTrains();
        assertEquals(HttpStatus.OK, response.getStatusCode()); assertEquals(schedules, response.getBody()); verify(trainScheduleService).getAllSchedules();
    }
    @Test void createTrainScheduleReturnsCreatedScheduleWithCoachSeats() {
        TrainScheduleRequest request = new TrainScheduleRequest("12345", "Delhi", "Mumbai",
                null, java.time.LocalDate.now().plusDays(1),
                List.of(new CoachConfigurationRequest("SL", 2, 72)));
        TrainSchedule schedule = new TrainSchedule();
        when(trainScheduleService.createSchedule(request)).thenReturn(schedule);
        ResponseEntity<TrainSchedule> response = controller.createTrain(request);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(schedule, response.getBody());
        verify(trainScheduleService).createSchedule(request);
    }
    @Test void updateTrainCoachesReturnsUpdatedSchedule() {
        List<CoachConfigurationRequest> coaches = List.of(new CoachConfigurationRequest("3AC", 1, 64));
        TrainSchedule schedule = new TrainSchedule();
        when(trainScheduleService.updateCoachConfiguration(9L, coaches)).thenReturn(schedule);
        ResponseEntity<TrainSchedule> response = controller.updateTrainCoaches(9L, coaches);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(schedule, response.getBody());
        verify(trainScheduleService).updateCoachConfiguration(9L, coaches);
    }
    @Test void updateTrainFareReturnsUpdatedSchedule() {
        TicketFareRequest request=new TicketFareRequest(new BigDecimal("275.50"));
        TrainSchedule schedule=new TrainSchedule();
        when(trainScheduleService.updateTicketFare(9L,request.ticketFare())).thenReturn(schedule);
        ResponseEntity<TrainSchedule> response=controller.updateTrainFare(9L,request);
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(schedule,response.getBody());
        verify(trainScheduleService).updateTicketFare(9L,request.ticketFare());
    }
}
