package com.sts.binding;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sts.registration.Registration;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;

@Entity
public class Ticket {
	@Id
	@GeneratedValue
	private Integer ticketId;
	private String ticketStatus;
	private String trainNo;
	private String name;
	private String dob;
	private String gender;
	private String doj;
	private String source;
	private String dest;
	@Column(name = "ticket_fare", precision = 12, scale = 2)
	private BigDecimal ticketFare;
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "registration_id")
	private Registration registration;
	
	public Integer getTicketId() {
		return ticketId;
	}
	public void setTicketId(Integer ticketId) {
		this.ticketId = ticketId;
	}
	public String getTicketStatus() {
		return ticketStatus;
	}
	public void setTicketStatus(String ticketStatus) {
		this.ticketStatus = ticketStatus;
	}
	public String getTrainNo() {
		return trainNo;
	}
	public void setTrainNo(String trainNo) {
		this.trainNo = trainNo;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDob() {
		return dob;
	}
	public void setDob(String dob) {
		this.dob = dob;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getDoj() {
		return doj;
	}
	public void setDoj(String doj) {
		this.doj = doj;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getDest() {
		return dest;
	}
	public void setDest(String dest) {
		this.dest = dest;
	}
	public BigDecimal getTicketFare() {
		return ticketFare == null ? new BigDecimal("100.00") : ticketFare;
	}
	public void setTicketFare(BigDecimal ticketFare) {
		this.ticketFare = ticketFare;
	}
	public Registration getRegistration() { return registration; }
	public void setRegistration(Registration registration) { this.registration = registration; }
	
}
