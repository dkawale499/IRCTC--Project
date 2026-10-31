package com.sts.train;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TrainScheduleRequest(String trainNo, String source, String destination, Integer availableSeats,
        LocalDate journeyDate, List<CoachConfigurationRequest> coaches, BigDecimal ticketFare) {

    public TrainScheduleRequest(String trainNo, String source, String destination, Integer availableSeats,
            LocalDate journeyDate) {
        this(trainNo, source, destination, availableSeats, journeyDate, null, null);
    }

    public TrainScheduleRequest(String trainNo, String source, String destination, Integer availableSeats,
            LocalDate journeyDate, List<CoachConfigurationRequest> coaches) {
        this(trainNo, source, destination, availableSeats, journeyDate, coaches, null);
    }
}
