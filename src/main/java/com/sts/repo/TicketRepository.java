package com.sts.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sts.binding.Ticket;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Integer>{
    List<Ticket> findAllByRegistration_Id(Long registrationId);
    List<Ticket> findAllByRegistration_Login_EmailIgnoreCase(String email);
}
