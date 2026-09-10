package com.sts.Contoller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sts.binding.Ticket;
import com.sts.service.TicketService;
import com.sts.service.TrainScheduleService;
import com.sts.train.TrainSchedule;
import com.sts.train.TrainScheduleRequest;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final TicketService ticketService;
    private final TrainScheduleService trainScheduleService;

    public AdminController(TicketService ticketService, TrainScheduleService trainScheduleService) {
        this.ticketService = ticketService;
        this.trainScheduleService = trainScheduleService;
    }

    @GetMapping("/tickets")
    public ResponseEntity<List<Ticket>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @PostMapping("/trains")
    public ResponseEntity<TrainSchedule> createTrain(@RequestBody TrainScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trainScheduleService.createSchedule(request));
    }

    @GetMapping("/trains")
    public ResponseEntity<List<TrainSchedule>> getAllTrains() {
        return ResponseEntity.ok(trainScheduleService.getAllSchedules());
    }
}
