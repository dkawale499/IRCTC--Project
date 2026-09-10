package com.sts.Contoller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sts.service.TrainScheduleService;
import com.sts.train.TrainSchedule;

@RestController
@RequestMapping("/trains")
public class TrainController {

    private final TrainScheduleService trainScheduleService;

    public TrainController(TrainScheduleService trainScheduleService) {
        this.trainScheduleService = trainScheduleService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<TrainSchedule>> search(
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate journeyDate) {
        return ResponseEntity.ok(trainScheduleService.search(source, destination, journeyDate));
    }
}
