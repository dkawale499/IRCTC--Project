package com.sts.train;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "train_coaches")
public class TrainCoach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "train_schedule_id", nullable = false)
    private TrainSchedule trainSchedule;

    @Column(name = "coach_type", nullable = false)
    private String coachType;

    @Column(name = "coach_count", nullable = false)
    private Integer coachCount;

    @Column(name = "seats_per_coach", nullable = false)
    private Integer seatsPerCoach;

    public Long getId() { return id; }
    @JsonIgnore
    public TrainSchedule getTrainSchedule() { return trainSchedule; }
    public void setTrainSchedule(TrainSchedule trainSchedule) { this.trainSchedule = trainSchedule; }
    public String getCoachType() { return coachType; }
    public void setCoachType(String coachType) { this.coachType = coachType; }
    public Integer getCoachCount() { return coachCount; }
    public void setCoachCount(Integer coachCount) { this.coachCount = coachCount; }
    public Integer getSeatsPerCoach() { return seatsPerCoach; }
    public void setSeatsPerCoach(Integer seatsPerCoach) { this.seatsPerCoach = seatsPerCoach; }
    public long getTotalSeats() { return (long) coachCount * seatsPerCoach; }
}
