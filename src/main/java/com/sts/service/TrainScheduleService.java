package com.sts.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.sts.repo.TrainScheduleRepository;
import com.sts.train.CoachConfigurationRequest;
import com.sts.train.TrainCoach;
import com.sts.train.TrainSchedule;
import com.sts.train.TrainScheduleRequest;

@Service
public class TrainScheduleService {
    private static final Logger logger = LogManager.getLogger(TrainScheduleService.class);
    private final TrainScheduleRepository trainScheduleRepository;

    public TrainScheduleService(TrainScheduleRepository trainScheduleRepository) {
        this.trainScheduleRepository = trainScheduleRepository;
    }

    @Transactional
    public TrainSchedule createSchedule(TrainScheduleRequest request) {
        String trainNo = requiredValue(request.trainNo(), "trainNo");
        String source = requiredValue(request.source(), "source");
        String destination = requiredValue(request.destination(), "destination");
        if (request.journeyDate() == null || request.journeyDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("journeyDate must be today or a future date");
        }
        if (trainScheduleRepository.existsByTrainNoAndJourneyDate(trainNo, request.journeyDate())) {
            throw new IllegalArgumentException("A schedule already exists for this train and date");
        }
        TrainSchedule schedule = new TrainSchedule();
        schedule.setTrainNo(trainNo);
        schedule.setSource(source);
        schedule.setDestination(destination);
        schedule.setJourneyDate(request.journeyDate());
        schedule.setTicketFare(validatedFare(request.ticketFare()));
        if (request.coaches() == null || request.coaches().isEmpty()) {
            schedule.setAvailableSeats(requiredAvailableSeats(request.availableSeats()));
        } else {
            applyCoachConfiguration(schedule, request.coaches());
        }
        TrainSchedule savedSchedule = trainScheduleRepository.save(schedule);
        logger.info("Train schedule created: trainNo={}, source={}, destination={}, date={}, seats={}, coachTypes={}",
                trainNo, source, destination, request.journeyDate(), schedule.getAvailableSeats(),
                schedule.getCoaches().size());
        return savedSchedule;
    }

    @Transactional
    public TrainSchedule updateTicketFare(Long scheduleId, BigDecimal ticketFare) {
        if (ticketFare == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ticketFare is required");
        }
        TrainSchedule schedule = trainScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Train schedule was not found"));
        schedule.setTicketFare(validatedFare(ticketFare));
        return trainScheduleRepository.save(schedule);
    }

    public BigDecimal getTicketFare(String trainNo, LocalDate journeyDate) {
        String normalizedTrainNo = requiredValue(trainNo, "trainNo");
        if (journeyDate == null) {
            throw new IllegalArgumentException("journeyDate is required");
        }
        return trainScheduleRepository.findByTrainNoIgnoreCaseAndJourneyDate(normalizedTrainNo, journeyDate)
                .map(TrainSchedule::getTicketFare)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No scheduled fare was found for this train and date"));
    }

    @Transactional
    public TrainSchedule updateCoachConfiguration(Long scheduleId, List<CoachConfigurationRequest> coaches) {
        if (coaches == null || coaches.isEmpty()) {
            throw new IllegalArgumentException("At least one coach configuration is required");
        }
        TrainSchedule schedule = trainScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Train schedule was not found"));
        applyCoachConfiguration(schedule, coaches);
        TrainSchedule savedSchedule = trainScheduleRepository.save(schedule);
        logger.info("Train coach configuration updated: scheduleId={}, seats={}, coachTypes={}", scheduleId,
                schedule.getAvailableSeats(), schedule.getCoaches().size());
        return savedSchedule;
    }

    public List<TrainSchedule> getAllSchedules() {
        List<TrainSchedule> schedules = trainScheduleRepository.findAll();
        logger.debug("Retrieved {} train schedules", schedules.size());
        return schedules;
    }

    public List<TrainSchedule> search(String source, String destination, LocalDate journeyDate) {
        String normalizedSource = source.trim();
        String normalizedDestination = destination.trim();
        logger.debug("Searching train schedules for date={}", journeyDate);
        List<TrainSchedule> schedules = trainScheduleRepository
                .findBySourceIgnoreCaseAndDestinationIgnoreCaseAndJourneyDate(normalizedSource, normalizedDestination,
                        journeyDate);
        logger.info("Train schedule search completed with {} result(s)", schedules.size());
        return schedules;
    }

    private String requiredValue(String value, String fieldName) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(fieldName + " is required");
        return value.trim();
    }

    private Integer requiredAvailableSeats(Integer availableSeats) {
        if (availableSeats == null || availableSeats < 0) {
            throw new IllegalArgumentException("availableSeats must be zero or greater");
        }
        return availableSeats;
    }

    private BigDecimal validatedFare(BigDecimal ticketFare) {
        BigDecimal fare = ticketFare == null ? new BigDecimal("100.00") : ticketFare;
        try {
            fare = fare.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "ticketFare must have no more than two decimal places", exception);
        }
        if (fare.signum() <= 0 || fare.precision() > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "ticketFare must be a positive amount with at most 10 integer digits");
        }
        return fare;
    }

    private void applyCoachConfiguration(TrainSchedule schedule, List<CoachConfigurationRequest> coachRequests) {
        Set<String> configuredTypes = new HashSet<>();
        List<TrainCoach> coaches = coachRequests.stream().map(request -> {
            String coachType = requiredValue(request.coachType(), "coachType").toUpperCase();
            if (!Set.of("SL", "3AC", "2AC", "1AC").contains(coachType)) {
                throw new IllegalArgumentException("coachType must be one of SL, 3AC, 2AC, or 1AC");
            }
            if (!configuredTypes.add(coachType)) {
                throw new IllegalArgumentException("Each coachType can only be configured once");
            }
            if (request.coachCount() == null || request.coachCount() <= 0) {
                throw new IllegalArgumentException("coachCount must be greater than zero");
            }
            if (request.seatsPerCoach() == null || request.seatsPerCoach() <= 0) {
                throw new IllegalArgumentException("seatsPerCoach must be greater than zero");
            }
            TrainCoach coach = new TrainCoach();
            coach.setCoachType(coachType);
            coach.setCoachCount(request.coachCount());
            coach.setSeatsPerCoach(request.seatsPerCoach());
            coach.setTrainSchedule(schedule);
            return coach;
        }).toList();
        long totalSeatCount = coaches.stream().mapToLong(TrainCoach::getTotalSeats).sum();
        if (totalSeatCount <= 0 || totalSeatCount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Coach configuration must provide available seats");
        }
        schedule.setCoaches(coaches);
        schedule.setAvailableSeats((int) totalSeatCount);
    }
}
