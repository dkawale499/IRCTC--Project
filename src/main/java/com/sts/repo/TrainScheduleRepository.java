package com.sts.repo;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sts.train.TrainSchedule;

public interface TrainScheduleRepository extends JpaRepository<TrainSchedule, Long> {
    boolean existsByTrainNoAndJourneyDate(String trainNo, LocalDate journeyDate);
    List<TrainSchedule> findBySourceIgnoreCaseAndDestinationIgnoreCaseAndJourneyDate(String source, String destination,
            LocalDate journeyDate);
}
