package com.sts.Contoller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.sts.binding.Ticket;
import com.sts.binding.TicketStatus;
import com.sts.binding.TicketStatusSummary;
import com.sts.registration.RegisteredUser;
import com.sts.registration.RegistrationRequest;
import com.sts.service.RegistrationService;
import com.sts.service.TicketService;
import com.sts.service.TrainScheduleService;
import com.sts.train.CoachConfigurationRequest;
import com.sts.train.TrainSchedule;
import com.sts.train.TrainScheduleRequest;
import com.sts.train.TicketFareRequest;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final TicketService ticketService;
    private final TrainScheduleService trainScheduleService;
    private final RegistrationService registrationService;

    public AdminController(TicketService ticketService, TrainScheduleService trainScheduleService,
            RegistrationService registrationService) {
        this.ticketService = ticketService;
        this.trainScheduleService = trainScheduleService;
        this.registrationService = registrationService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<RegisteredUser>> getRegisteredUsers() {
        return ResponseEntity.ok(registrationService.getRegisteredUsers());
    }

    @PostMapping("/users")
    public ResponseEntity<RegisteredUser> createRegisteredUser(@RequestBody RegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registrationService.createRegisteredUser(request));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteRegisteredUser(@PathVariable Long userId) {
        registrationService.deleteRegisteredUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tickets")
    public ResponseEntity<List<Ticket>> getAllTickets(@RequestParam(required = false) String status) {
        if (status == null) {
            return ResponseEntity.ok(ticketService.getAllTickets());
        }
        return TicketStatus.from(status)
                .map(ticketStatus -> ResponseEntity.ok(ticketService.getAllTickets(ticketStatus)))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping("/tickets/summary")
    public ResponseEntity<TicketStatusSummary> getTicketStatusSummary() {
        return ResponseEntity.ok(ticketService.getTicketStatusSummary());
    }

    @PostMapping("/trains")
    public ResponseEntity<TrainSchedule> createTrain(@RequestBody TrainScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trainScheduleService.createSchedule(request));
    }

    @PutMapping("/trains/{scheduleId}/coaches")
    public ResponseEntity<TrainSchedule> updateTrainCoaches(@PathVariable Long scheduleId,
            @RequestBody List<CoachConfigurationRequest> coaches) {
        return ResponseEntity.ok(trainScheduleService.updateCoachConfiguration(scheduleId, coaches));
    }

    @PutMapping("/trains/{scheduleId}/fare")
    public ResponseEntity<TrainSchedule> updateTrainFare(@PathVariable Long scheduleId,
            @RequestBody TicketFareRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ticketFare is required");
        }
        return ResponseEntity.ok(trainScheduleService.updateTicketFare(scheduleId, request.ticketFare()));
    }

    @GetMapping("/trains")
    public ResponseEntity<List<TrainSchedule>> getAllTrains() {
        return ResponseEntity.ok(trainScheduleService.getAllSchedules());
    }
}
