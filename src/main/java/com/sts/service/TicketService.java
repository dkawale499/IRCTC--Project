package com.sts.service;
import java.util.List;
import com.sts.binding.Passenger;
import com.sts.binding.Ticket;
import com.sts.binding.TicketStatus;
import com.sts.binding.TicketStatusSummary;

public interface TicketService {
	public Ticket bookTicket(Passenger passenger, String email);
	 
	public Ticket getTicket(Integer ticketId);

	public Ticket cancelTicket(Integer ticketId, String email, boolean administrator);
	
	public List<Ticket>getAllTickets();

	public List<Ticket> getPassengerTickets(String email);

	public List<Ticket> getAllTickets(TicketStatus status);

	public TicketStatusSummary getTicketStatusSummary();
}
