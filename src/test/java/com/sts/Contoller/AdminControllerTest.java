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
import com.sts.service.*;
import com.sts.train.TrainSchedule;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {
    @Mock private TicketService ticketService;
    @Mock private TrainScheduleService trainScheduleService;
    @InjectMocks private AdminController controller;
    @Test void getAllTicketsReturnsTicketsForAdminRoute() {
        List<Ticket> tickets = List.of(new Ticket()); when(ticketService.getAllTickets()).thenReturn(tickets);
        ResponseEntity<List<Ticket>> response = controller.getAllTickets();
        assertEquals(HttpStatus.OK, response.getStatusCode()); assertEquals(tickets, response.getBody()); verify(ticketService).getAllTickets();
    }
    @Test void getAllTrainsReturnsSchedules() {
        List<TrainSchedule> schedules = List.of(new TrainSchedule()); when(trainScheduleService.getAllSchedules()).thenReturn(schedules);
        ResponseEntity<List<TrainSchedule>> response = controller.getAllTrains();
        assertEquals(HttpStatus.OK, response.getStatusCode()); assertEquals(schedules, response.getBody()); verify(trainScheduleService).getAllSchedules();
    }
}
