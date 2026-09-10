package com.sts.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.sts.binding.Passenger;
import com.sts.binding.Ticket;
import com.sts.repo.TicketRepository;
@Service
public class TickerServiceImpl implements TicketService {

	private static final Logger logger = LogManager.getLogger(TickerServiceImpl.class);

	@Autowired
	private TicketRepository repo;
	
	@Override
	public Ticket bookTicket(Passenger passenger) {
		logger.info("Booking ticket for trainNo={}, source={}, destination={}, journeyDate={}",
				passenger.getTrainNo(), passenger.getSource(), passenger.getDest(), passenger.getDoj());
		Ticket  t= new Ticket();
		BeanUtils.copyProperties(passenger, t);
		t.setTicketStatus("Confirmed");
		Ticket savedticket=repo.save(t);
		logger.info("Ticket booked successfully with ticketId={}, status={}", savedticket.getTicketId(),
				savedticket.getTicketStatus());
		return savedticket;
	}

	@Override
	public Ticket getTicket(Integer ticketId) {
		logger.debug("Looking up ticket with ticketId={}", ticketId);
		return repo.findById(ticketId).orElseThrow(() -> {
			logger.warn("Ticket not found with ticketId={}", ticketId);
			return new NoSuchElementException();
		});
		
	}

	@Override
	public Ticket cancelTicket(Integer ticketId) {
		Ticket ticket = getTicket(ticketId);
		if ("Cancelled".equalsIgnoreCase(ticket.getTicketStatus())) {
			logger.warn("Ticket cancellation rejected because ticketId={} is already cancelled", ticketId);
			throw new IllegalStateException("Ticket is already cancelled");
		}

		ticket.setTicketStatus("Cancelled");
		Ticket cancelledTicket = repo.save(ticket);
		logger.info("Ticket cancelled successfully with ticketId={}", ticketId);
		return cancelledTicket;
	}

	@Override
	public List<Ticket> getAllTickets() {
		List<Ticket> tickets = repo.findAll();
		logger.debug("Retrieved {} tickets", tickets.size());
		return tickets;
		
	}

}
