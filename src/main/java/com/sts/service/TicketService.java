package com.sts.service;
import java.util.List;
import com.sts.binding.Passenger;
import com.sts.binding.Ticket;

public interface TicketService {
	public Ticket bookTicket(Passenger passenger);
	 
	public Ticket getTicket(Integer ticketId);

	public Ticket cancelTicket(Integer ticketId);
	
	public List<Ticket>getAllTickets();
}
