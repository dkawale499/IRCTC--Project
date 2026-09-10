package com.sts.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import com.sts.repo.TrainScheduleRepository;
import com.sts.train.*;

@ExtendWith(MockitoExtension.class)
class TrainScheduleServiceTest {
    @Mock private TrainScheduleRepository repository;
    private TrainScheduleService service;
    @BeforeEach void setUp(){service=new TrainScheduleService(repository);}
    @Test void createScheduleStoresDateWiseAvailability(){ LocalDate date=LocalDate.now().plusDays(10); when(repository.existsByTrainNoAndJourneyDate("12345",date)).thenReturn(false); when(repository.save(any())).thenAnswer(i->i.getArgument(0)); TrainSchedule s=service.createSchedule(new TrainScheduleRequest("12345","Delhi","Mumbai",500,date)); assertEquals("12345",s.getTrainNo()); assertEquals("Delhi",s.getSource()); assertEquals("Mumbai",s.getDestination()); assertEquals(500,s.getAvailableSeats()); assertEquals(date,s.getJourneyDate()); }
    @Test void createScheduleRejectsDuplicateTrainAndDate(){ LocalDate date=LocalDate.now().plusDays(10); when(repository.existsByTrainNoAndJourneyDate("12345",date)).thenReturn(true); assertThrows(IllegalArgumentException.class,()->service.createSchedule(new TrainScheduleRequest("12345","Delhi","Mumbai",500,date))); verify(repository,never()).save(any()); }
    @Test void createScheduleRejectsNegativeSeats(){ assertThrows(IllegalArgumentException.class,()->service.createSchedule(new TrainScheduleRequest("12345","Delhi","Mumbai",-1,LocalDate.now().plusDays(10)))); }
    @Test void createScheduleRejectsPastDate(){ assertThrows(IllegalArgumentException.class,()->service.createSchedule(new TrainScheduleRequest("12345","Delhi","Mumbai",500,LocalDate.now().minusDays(1)))); }
    @Test void searchUsesSourceDestinationAndDate(){ LocalDate date=LocalDate.now().plusDays(10); List<TrainSchedule> schedules=List.of(new TrainSchedule()); when(repository.findBySourceIgnoreCaseAndDestinationIgnoreCaseAndJourneyDate("Delhi","Mumbai",date)).thenReturn(schedules); assertEquals(schedules,service.search(" Delhi "," Mumbai ",date)); }
}
