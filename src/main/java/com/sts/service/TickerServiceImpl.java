package com.sts.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.time.LocalDate;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.sts.binding.Passenger;
import com.sts.binding.Ticket;
import com.sts.binding.TicketStatus;
import com.sts.binding.TicketStatusSummary;
import com.sts.registration.Registration;
import com.sts.repo.TicketRepository;
import com.sts.repo.TrainScheduleRepository;
import com.sts.train.TrainSchedule;
@Service
public class TickerServiceImpl implements TicketService {

	private static final Logger logger = LogManager.getLogger(TickerServiceImpl.class);

	@Autowired
	private TicketRepository repo;

	@Autowired
	private WalletService walletService;

	@Autowired
	private TrainScheduleRepository trainScheduleRepository;
	@Override
	@Transactional
	public Ticket bookTicket(Passenger passenger, String email) {
		LocalDate journeyDate;
		try {
			journeyDate = LocalDate.parse(passenger.getDoj());
		} catch (RuntimeException exception) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Journey date must use yyyy-MM-dd format.");
		}
		TrainSchedule schedule = trainScheduleRepository
				.findByTrainNoIgnoreCaseAndJourneyDate(passenger.getTrainNo(), journeyDate)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"No scheduled fare was found for this train and date."));
		if (!schedule.getSource().equalsIgnoreCase(passenger.getSource())
				|| !schedule.getDestination().equalsIgnoreCase(passenger.getDest())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Journey stations do not match the selected train schedule.");
		}
		Registration owner = walletService.lockAndValidateTicketPayment(email, schedule.getTicketFare());
		Ticket ticket = new Ticket();
		BeanUtils.copyProperties(passenger, ticket);
		ticket.setTicketFare(schedule.getTicketFare());
		ticket.setTicketStatus("Confirmed");
		ticket.setRegistration(owner);
		Ticket savedTicket = repo.save(ticket);
		walletService.debitTicketFare(owner, savedTicket);
		logger.info("Ticket booked using passenger wallet, ticketId={}", savedTicket.getTicketId());
		return savedTicket;
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
	@Transactional
	public Ticket cancelTicket(Integer ticketId, String email, boolean administrator) {
		Ticket ticket = getTicket(ticketId);
		Registration owner = ticket.getRegistration();
		if (!administrator && (owner == null || owner.getLogin() == null
				|| !owner.getLogin().getEmail().equalsIgnoreCase(email))) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN,
					"You can only cancel tickets booked from your account.");
		}
		if ("Cancelled".equalsIgnoreCase(ticket.getTicketStatus())) {
			throw new IllegalStateException("Ticket is already cancelled");
		}

		ticket.setTicketStatus("Cancelled");
		Ticket cancelledTicket = repo.save(ticket);
		if (owner != null) {
			walletService.refundTicketFare(owner, ticket);
		}
		return cancelledTicket;
	}

	@Override
	public List<Ticket> getAllTickets() {
		List<Ticket> tickets = repo.findAll();
		logger.debug("Retrieved {} tickets", tickets.size());
		return tickets;
		
	}

	@Override
	public List<Ticket> getPassengerTickets(String email) {
		return repo.findAllByRegistration_Login_EmailIgnoreCase(email);
	}

	@Override
	public List<Ticket> getAllTickets(TicketStatus status) {
		List<Ticket> tickets = getAllTickets();
		if (status == null) {
			return tickets;
		}
		return tickets.stream()
				.filter(ticket -> status.matches(ticket.getTicketStatus()))
				.toList();
	}

	@Override
	public TicketStatusSummary getTicketStatusSummary() {
		return TicketStatusSummary.from(getAllTickets());
	}

}
