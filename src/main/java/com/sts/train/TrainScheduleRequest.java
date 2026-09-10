package com.sts.train;

import java.time.LocalDate;

public record TrainScheduleRequest(String trainNo, String source, String destination, Integer availableSeats,
        LocalDate journeyDate) {
}
