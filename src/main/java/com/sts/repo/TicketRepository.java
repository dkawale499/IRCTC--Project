package com.sts.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sts.binding.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Integer>{

}
