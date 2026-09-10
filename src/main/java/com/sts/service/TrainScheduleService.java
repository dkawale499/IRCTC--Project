package com.sts.service;

import java.time.LocalDate;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sts.repo.TrainScheduleRepository;
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
        if (request.availableSeats() == null || request.availableSeats() < 0) {
            throw new IllegalArgumentException("availableSeats must be zero or greater");
        }
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
        schedule.setAvailableSeats(request.availableSeats());
        schedule.setJourneyDate(request.journeyDate());
        TrainSchedule savedSchedule = trainScheduleRepository.save(schedule);
        logger.info("Train schedule created: trainNo={}, source={}, destination={}, date={}, seats={}", trainNo, source,
                destination, request.journeyDate(), request.availableSeats());
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
}
