package com.sts.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import com.sts.repo.TrainScheduleRepository;
import com.sts.train.CoachConfigurationRequest;
import com.sts.train.*;

@ExtendWith(MockitoExtension.class)
class TrainScheduleServiceTest {
    @Mock private TrainScheduleRepository repository;
    private TrainScheduleService service;
    @BeforeEach void setUp(){service=new TrainScheduleService(repository);}
    @Test void createScheduleStoresDateWiseAvailability(){ LocalDate date=LocalDate.now().plusDays(10); when(repository.existsByTrainNoAndJourneyDate("12345",date)).thenReturn(false); when(repository.save(any())).thenAnswer(i->i.getArgument(0)); TrainSchedule s=service.createSchedule(new TrainScheduleRequest("12345","Delhi","Mumbai",500,date)); assertEquals("12345",s.getTrainNo()); assertEquals("Delhi",s.getSource()); assertEquals("Mumbai",s.getDestination()); assertEquals(500,s.getAvailableSeats()); assertEquals(date,s.getJourneyDate()); }
    @Test void createScheduleStoresAdminDefinedTicketFare(){ LocalDate date=LocalDate.now().plusDays(10); when(repository.existsByTrainNoAndJourneyDate("12345",date)).thenReturn(false); when(repository.save(any())).thenAnswer(i->i.getArgument(0)); TrainSchedule s=service.createSchedule(new TrainScheduleRequest("12345","Delhi","Mumbai",500,date,null,new BigDecimal("275.50"))); assertEquals(new BigDecimal("275.50"),s.getTicketFare()); }
    @Test void createScheduleDefaultsFareForOlderClients(){ LocalDate date=LocalDate.now().plusDays(10); when(repository.existsByTrainNoAndJourneyDate("12345",date)).thenReturn(false); when(repository.save(any())).thenAnswer(i->i.getArgument(0)); assertEquals(new BigDecimal("100.00"),service.createSchedule(new TrainScheduleRequest("12345","Delhi","Mumbai",500,date)).getTicketFare()); }
    @Test void createScheduleRejectsNonPositiveFare(){ LocalDate date=LocalDate.now().plusDays(10); when(repository.existsByTrainNoAndJourneyDate("12345",date)).thenReturn(false); assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST,assertThrows(org.springframework.web.server.ResponseStatusException.class,()->service.createSchedule(new TrainScheduleRequest("12345","Delhi","Mumbai",500,date,null,BigDecimal.ZERO))).getStatusCode()); verify(repository,never()).save(any()); }
    @Test void createScheduleRejectsFareWithExcessPrecision(){ LocalDate date=LocalDate.now().plusDays(10); when(repository.existsByTrainNoAndJourneyDate("12345",date)).thenReturn(false); assertThrows(org.springframework.web.server.ResponseStatusException.class,()->service.createSchedule(new TrainScheduleRequest("12345","Delhi","Mumbai",500,date,null,new BigDecimal("1.234")))); verify(repository,never()).save(any()); }
    @Test void createScheduleRejectsDuplicateTrainAndDate(){ LocalDate date=LocalDate.now().plusDays(10); when(repository.existsByTrainNoAndJourneyDate("12345",date)).thenReturn(true); assertThrows(IllegalArgumentException.class,()->service.createSchedule(new TrainScheduleRequest("12345","Delhi","Mumbai",500,date))); verify(repository,never()).save(any()); }
    @Test void createScheduleRejectsNegativeSeats(){ assertThrows(IllegalArgumentException.class,()->service.createSchedule(new TrainScheduleRequest("12345","Delhi","Mumbai",-1,LocalDate.now().plusDays(10)))); }
    @Test void createScheduleRejectsPastDate(){ assertThrows(IllegalArgumentException.class,()->service.createSchedule(new TrainScheduleRequest("12345","Delhi","Mumbai",500,LocalDate.now().minusDays(1)))); }
    @Test void createScheduleBuildsCoachConfigurationAndCalculatesSeats(){ LocalDate date=LocalDate.now().plusDays(10); when(repository.existsByTrainNoAndJourneyDate("12345",date)).thenReturn(false); when(repository.save(any())).thenAnswer(i->i.getArgument(0)); TrainSchedule s=service.createSchedule(new TrainScheduleRequest("12345","Delhi","Mumbai",null,date,List.of(new CoachConfigurationRequest("SL",2,72),new CoachConfigurationRequest("3AC",1,64)))); assertEquals(208,s.getAvailableSeats()); assertEquals(2,s.getCoaches().size()); assertEquals("SL",s.getCoaches().get(0).getCoachType()); assertEquals(144,s.getCoaches().get(0).getTotalSeats()); }
    @Test void createScheduleRejectsInvalidCoachType(){ LocalDate date=LocalDate.now().plusDays(10); when(repository.existsByTrainNoAndJourneyDate("12345",date)).thenReturn(false); assertThrows(IllegalArgumentException.class,()->service.createSchedule(new TrainScheduleRequest("12345","Delhi","Mumbai",null,date,List.of(new CoachConfigurationRequest("Chair car",1,72))))); verify(repository,never()).save(any()); }
    @Test void createScheduleRejectsDuplicateCoachTypes(){ LocalDate date=LocalDate.now().plusDays(10); when(repository.existsByTrainNoAndJourneyDate("12345",date)).thenReturn(false); assertThrows(IllegalArgumentException.class,()->service.createSchedule(new TrainScheduleRequest("12345","Delhi","Mumbai",null,date,List.of(new CoachConfigurationRequest("SL",1,72),new CoachConfigurationRequest("sl",1,72))))); verify(repository,never()).save(any()); }
    @Test void updateCoachConfigurationReplacesCoachesAndSeatTotal(){ TrainSchedule schedule=new TrainSchedule(); schedule.setAvailableSeats(50); when(repository.findById(8L)).thenReturn(java.util.Optional.of(schedule)); when(repository.save(schedule)).thenReturn(schedule); TrainSchedule updated=service.updateCoachConfiguration(8L,List.of(new CoachConfigurationRequest("2AC",2,46),new CoachConfigurationRequest("1AC",1,24))); assertEquals(116,updated.getAvailableSeats()); assertEquals(2,updated.getCoaches().size()); verify(repository).save(schedule); }
    @Test void searchUsesSourceDestinationAndDate(){ LocalDate date=LocalDate.now().plusDays(10); List<TrainSchedule> schedules=List.of(new TrainSchedule()); when(repository.findBySourceIgnoreCaseAndDestinationIgnoreCaseAndJourneyDate("Delhi","Mumbai",date)).thenReturn(schedules); assertEquals(schedules,service.search(" Delhi "," Mumbai ",date)); }
    @Test void getTicketFareUsesTrainAndJourneyDate(){ LocalDate date=LocalDate.now().plusDays(10); TrainSchedule schedule=new TrainSchedule(); schedule.setTicketFare(new BigDecimal("275.50")); when(repository.findByTrainNoIgnoreCaseAndJourneyDate("12345",date)).thenReturn(java.util.Optional.of(schedule)); assertEquals(new BigDecimal("275.50"),service.getTicketFare(" 12345 ",date)); }
    @Test void getTicketFareRejectsUnknownSchedule(){ LocalDate date=LocalDate.now().plusDays(10); when(repository.findByTrainNoIgnoreCaseAndJourneyDate("12345",date)).thenReturn(java.util.Optional.empty()); assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST,assertThrows(org.springframework.web.server.ResponseStatusException.class,()->service.getTicketFare("12345",date)).getStatusCode()); }
    @Test void updateTicketFareChangesScheduleFare(){ TrainSchedule schedule=new TrainSchedule(); when(repository.findById(8L)).thenReturn(java.util.Optional.of(schedule)); when(repository.save(schedule)).thenReturn(schedule); assertEquals(new BigDecimal("275.50"),service.updateTicketFare(8L,new BigDecimal("275.50")).getTicketFare()); verify(repository).save(schedule); }
    @Test void updateTicketFareRejectsNull(){ assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST,assertThrows(org.springframework.web.server.ResponseStatusException.class,()->service.updateTicketFare(8L,null)).getStatusCode()); verify(repository,never()).save(any()); }
}
