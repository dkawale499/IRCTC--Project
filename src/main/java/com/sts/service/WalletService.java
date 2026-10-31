package com.sts.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.sts.binding.Ticket;
import com.sts.registration.Registration;
import com.sts.repo.RegistrationRepository;
import com.sts.wallet.WalletSummary;
import com.sts.wallet.WalletTransaction;
import com.sts.wallet.WalletTransactionRepository;
import com.sts.wallet.WalletTransactionView;

@Service
public class WalletService {
    public static final BigDecimal SIGNUP_BONUS = new BigDecimal("100.00");
    public static final BigDecimal TICKET_FARE = new BigDecimal("100.00");

    private final RegistrationRepository registrationRepository;
    private final WalletTransactionRepository transactionRepository;

    public WalletService(RegistrationRepository registrationRepository,
            WalletTransactionRepository transactionRepository) {
        this.registrationRepository = registrationRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public void addSignupBonus(Registration registration) {
        registration.setWalletBalance(SIGNUP_BONUS);
        registration.setRegisteredAt(LocalDateTime.now());
        registration.setWalletInitialized(true);
        transactionRepository.save(createTransaction(registration, "CREDIT", "Welcome signup bonus",
                SIGNUP_BONUS, SIGNUP_BONUS, null));
    }

    @Transactional
    public WalletSummary getWallet(String email) {
        Registration registration = registrationRepository.findByEmailForUpdate(email)
                .filter(account -> "USER".equalsIgnoreCase(account.getLogin().getRole()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Passenger wallet not found."));
        List<WalletTransaction> transactions =
                transactionRepository.findAllByRegistration_IdOrderByCreatedAtDesc(registration.getId());
        if (!registration.isWalletInitialized()) {
            if (transactions.isEmpty()) {
                addSignupBonus(registration);
            } else {
                registration.setWalletInitialized(true);
                if (registration.getRegisteredAt() == null) {
                    registration.setRegisteredAt(LocalDateTime.now());
                }
            }
            registrationRepository.save(registration);
            if (transactions.isEmpty()) {
                transactions = transactionRepository.findAllByRegistration_IdOrderByCreatedAtDesc(registration.getId());
            }
        }
        BigDecimal signupCreditTotal = transactions.stream()
                .filter(transaction -> "CREDIT".equals(transaction.getType())
                        && "Welcome signup bonus".equalsIgnoreCase(transaction.getDescription()))
                .map(WalletTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<WalletTransactionView> views = transactions.stream()
                .map(transaction -> new WalletTransactionView(
                        transaction.getId(),
                        transaction.getType(),
                        transaction.getDescription(),
                        transaction.getCreatedAt(),
                        transaction.getAmount(),
                        transaction.getBalanceAfter(),
                        "Success"))
                .toList();
        return new WalletSummary(registration.getWalletBalance(), signupCreditTotal, transactions.size(),
                registration.getFullName(), registration.getEmail(), registration.getPhone(),
                registration.getRegisteredAt(), views);
    }

    @Transactional
    public Registration lockAndValidateTicketPayment(String email) {
        return lockAndValidateTicketPayment(email, TICKET_FARE);
    }

    @Transactional
    public Registration lockAndValidateTicketPayment(String email, BigDecimal ticketFare) {
        Registration registration = registrationRepository.findByEmailForUpdate(email)
                .filter(account -> "USER".equalsIgnoreCase(account.getLogin().getRole()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Passenger wallet is unavailable."));
        if (registration.getWalletBalance().compareTo(ticketFare) < 0) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED,
                    "Insufficient wallet balance. Ticket fare is ₹" + ticketFare.toPlainString()
                            + "; available balance is ₹" + registration.getWalletBalance().toPlainString() + ".");
        }
        return registration;
    }

    @Transactional
    public void debitTicketFare(Registration registration, Ticket ticket) {
        BigDecimal ticketFare = ticket.getTicketFare();
        BigDecimal balance = registration.getWalletBalance().subtract(ticketFare);
        registration.setWalletBalance(balance);
        registrationRepository.save(registration);
        transactionRepository.save(createTransaction(registration, "DEBIT",
                "Ticket booking PNR " + ticket.getTicketId(), ticketFare, balance, ticket.getTicketId()));
    }

    @Transactional
    public void refundTicketFare(Registration owner, Ticket ticket) {
        Registration registration = registrationRepository.findByIdForUpdate(owner.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Passenger wallet not found."));
        BigDecimal ticketFare = ticket.getTicketFare();
        BigDecimal balance = registration.getWalletBalance().add(ticketFare);
        registration.setWalletBalance(balance);
        registrationRepository.save(registration);
        transactionRepository.save(createTransaction(registration, "REFUND",
                "Refund for cancelled ticket PNR " + ticket.getTicketId(), ticketFare, balance, ticket.getTicketId()));
    }

    private WalletTransaction createTransaction(Registration registration, String type, String description,
            BigDecimal amount, BigDecimal balanceAfter, Integer ticketId) {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setRegistration(registration);
        transaction.setType(type);
        transaction.setDescription(description);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setTicketId(ticketId);
        transaction.setCreatedAt(LocalDateTime.now());
        return transaction;
    }
}
