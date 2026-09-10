package com.sts.Contoller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import com.sts.service.TrainScheduleService;
import com.sts.train.TrainSchedule;

@ExtendWith(MockitoExtension.class)
class TrainControllerTest {
    @Mock private TrainScheduleService trainScheduleService;
    @InjectMocks private TrainController controller;
    @Test void searchReturnsSchedules() {
        LocalDate date = LocalDate.of(2026, 10, 1); List<TrainSchedule> schedules = List.of(new TrainSchedule());
        when(trainScheduleService.search("Delhi", "Mumbai", date)).thenReturn(schedules);
        ResponseEntity<List<TrainSchedule>> response = controller.search("Delhi", "Mumbai", date);
        assertEquals(HttpStatus.OK, response.getStatusCode()); assertEquals(schedules, response.getBody()); verify(trainScheduleService).search("Delhi", "Mumbai", date);
    }
}
