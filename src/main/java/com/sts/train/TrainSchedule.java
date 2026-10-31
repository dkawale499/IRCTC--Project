package com.sts.train;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "train_schedules", uniqueConstraints = @UniqueConstraint(columnNames = { "train_no", "journey_date" }))
public class TrainSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "train_no", nullable = false)
    private String trainNo;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private String destination;

    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @Column(name = "journey_date", nullable = false)
    private LocalDate journeyDate;

    @Column(name = "ticket_fare", precision = 12, scale = 2)
    private BigDecimal ticketFare = new BigDecimal("100.00");

    @OneToMany(mappedBy = "trainSchedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrainCoach> coaches = new ArrayList<>();

    public Long getId() { return id; }
    public String getTrainNo() { return trainNo; }
    public void setTrainNo(String trainNo) { this.trainNo = trainNo; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public Integer getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }
    public LocalDate getJourneyDate() { return journeyDate; }
    public void setJourneyDate(LocalDate journeyDate) { this.journeyDate = journeyDate; }
    public BigDecimal getTicketFare() {
        return ticketFare == null ? new BigDecimal("100.00") : ticketFare;
    }
    public void setTicketFare(BigDecimal ticketFare) { this.ticketFare = ticketFare; }
    public List<TrainCoach> getCoaches() { return coaches; }
    public void setCoaches(List<TrainCoach> coaches) {
        this.coaches.clear();
        if (coaches != null) {
            coaches.forEach(coach -> {
                coach.setTrainSchedule(this);
                this.coaches.add(coach);
            });
        }
    }
}
