package com.sts.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.sts.binding.Ticket;
import com.sts.login.Login;
import com.sts.registration.Registration;
import com.sts.repo.RegistrationRepository;
import com.sts.wallet.WalletTransaction;
import com.sts.wallet.WalletTransactionRepository;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {
    @Mock private RegistrationRepository registrationRepository;
    @Mock private WalletTransactionRepository transactionRepository;
    private WalletService walletService;

    @BeforeEach
    void setUp() {
        walletService = new WalletService(registrationRepository, transactionRepository);
    }

    @Test
    void signupBonusSetsInitialBalanceAndCreatesCreditLedgerEntry() {
        Registration registration = new Registration();
        walletService.addSignupBonus(registration);
        assertEquals(new BigDecimal("100.00"), registration.getWalletBalance());
        ArgumentCaptor<WalletTransaction> transaction = ArgumentCaptor.forClass(WalletTransaction.class);
        verify(transactionRepository).save(transaction.capture());
        assertEquals("CREDIT", transaction.getValue().getType());
        assertEquals(new BigDecimal("100.00"), transaction.getValue().getAmount());
        assertEquals(new BigDecimal("100.00"), transaction.getValue().getBalanceAfter());
    }

    @Test
    void ticketPaymentLocksPassengerAndRejectsInsufficientBalance() {
        Registration registration = userRegistration("user@example.com", "99.99");
        when(registrationRepository.findByEmailForUpdate("user@example.com")).thenReturn(Optional.of(registration));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> walletService.lockAndValidateTicketPayment("user@example.com"));
        assertEquals(HttpStatus.PAYMENT_REQUIRED, exception.getStatusCode());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void ticketPaymentRejectsBalanceBelowConfiguredFare() {
        Registration registration = userRegistration("user@example.com", "250.00");
        when(registrationRepository.findByEmailForUpdate("user@example.com")).thenReturn(Optional.of(registration));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> walletService.lockAndValidateTicketPayment("user@example.com", new BigDecimal("275.50")));
        assertEquals(HttpStatus.PAYMENT_REQUIRED, exception.getStatusCode());
        org.junit.jupiter.api.Assertions.assertTrue(exception.getReason().contains("275.50"));
        org.junit.jupiter.api.Assertions.assertTrue(exception.getReason().contains("250.00"));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void ticketPaymentDebitsBalanceAndWritesLedger() {
        Registration registration = userRegistration("user@example.com", "150.00");
        Ticket ticket = new Ticket();
        ticket.setTicketId(41);
        walletService.debitTicketFare(registration, ticket);
        assertEquals(new BigDecimal("50.00"), registration.getWalletBalance());
        ArgumentCaptor<WalletTransaction> transaction = ArgumentCaptor.forClass(WalletTransaction.class);
        verify(transactionRepository).save(transaction.capture());
        assertEquals("DEBIT", transaction.getValue().getType());
        assertEquals(41, transaction.getValue().getTicketId());
        assertEquals(new BigDecimal("50.00"), transaction.getValue().getBalanceAfter());
    }

    @Test
    void cancellationRefundRestoresBalanceAndWritesRefundLedger() {
        Registration registration = userRegistration("user@example.com", "0.00");
        registration.setId(8L);
        when(registrationRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(registration));
        Ticket ticket = new Ticket();
        ticket.setTicketId(41);
        walletService.refundTicketFare(registration, ticket);
        assertEquals(new BigDecimal("100.00"), registration.getWalletBalance());
        ArgumentCaptor<WalletTransaction> transaction = ArgumentCaptor.forClass(WalletTransaction.class);
        verify(transactionRepository).save(transaction.capture());
        assertEquals("REFUND", transaction.getValue().getType());
        assertEquals(41, transaction.getValue().getTicketId());
    }

    @Test
    void bookingAndCancellationUseConfiguredFareInBalanceAndLedger() {
        Registration registration = userRegistration("user@example.com", "500.00");
        registration.setId(8L);
        Ticket ticket = new Ticket();
        ticket.setTicketId(42);
        ticket.setTicketFare(new BigDecimal("275.50"));

        walletService.debitTicketFare(registration, ticket);
        assertEquals(new BigDecimal("224.50"), registration.getWalletBalance());
        ArgumentCaptor<WalletTransaction> debit = ArgumentCaptor.forClass(WalletTransaction.class);
        verify(transactionRepository).save(debit.capture());
        assertEquals(new BigDecimal("275.50"), debit.getValue().getAmount());
        assertEquals(new BigDecimal("224.50"), debit.getValue().getBalanceAfter());

        when(registrationRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(registration));
        walletService.refundTicketFare(registration, ticket);
        assertEquals(new BigDecimal("500.00"), registration.getWalletBalance());
        ArgumentCaptor<WalletTransaction> refund = ArgumentCaptor.forClass(WalletTransaction.class);
        verify(transactionRepository, times(2)).save(refund.capture());
        assertEquals(new BigDecimal("275.50"), refund.getAllValues().get(1).getAmount());
        assertEquals(new BigDecimal("500.00"), refund.getAllValues().get(1).getBalanceAfter());
    }

    @Test
    void walletSummaryReturnsPassengerDetailsAndSignupCredits() {
        Registration registration = userRegistration("user@example.com", "75.00");
        registration.setId(8L);
        WalletTransaction credit = new WalletTransaction();
        credit.setType("CREDIT");
        credit.setDescription("Welcome signup bonus");
        credit.setAmount(new BigDecimal("100.00"));
        WalletTransaction refund = new WalletTransaction();
        refund.setType("REFUND");
        refund.setDescription("Ticket refund");
        refund.setAmount(new BigDecimal("100.00"));
        WalletTransaction debit = new WalletTransaction();
        debit.setType("DEBIT");
        debit.setAmount(new BigDecimal("25.00"));
        when(registrationRepository.findByEmailForUpdate("user@example.com"))
                .thenReturn(Optional.of(registration));
        when(transactionRepository.findAllByRegistration_IdOrderByCreatedAtDesc(8L))
                .thenReturn(List.of(refund, debit, credit));
        var summary = walletService.getWallet("user@example.com");
        assertEquals(new BigDecimal("75.00"), summary.balance());
        assertEquals(new BigDecimal("100.00"), summary.signupCreditTotal());
        assertEquals(3, summary.totalTransactions());
        assertEquals(3, summary.transactions().size());
        assertEquals("Passenger Name", summary.passengerName());
        assertEquals("user@example.com", summary.email());
        assertEquals("555-0100", summary.phone());
    }

    @Test
    void existingPassengerReceivesOneTimeSignupBonusOnFirstWalletAccess() {
        Registration registration = userRegistration("user@example.com", "0.00");
        registration.setId(8L);
        java.util.ArrayList<WalletTransaction> transactions = new java.util.ArrayList<>();
        when(registrationRepository.findByEmailForUpdate("user@example.com"))
                .thenReturn(Optional.of(registration));
        when(transactionRepository.findAllByRegistration_IdOrderByCreatedAtDesc(8L))
                .thenAnswer(invocation -> List.copyOf(transactions));
        when(transactionRepository.save(any(WalletTransaction.class))).thenAnswer(invocation -> {
            WalletTransaction transaction = invocation.getArgument(0);
            transaction.setId(1L);
            transactions.add(transaction);
            return transaction;
        });
        var summary = walletService.getWallet("user@example.com");
        assertEquals(new BigDecimal("100.00"), summary.balance());
        assertEquals(new BigDecimal("100.00"), summary.signupCreditTotal());
        assertEquals(1, summary.totalTransactions());
        verify(transactionRepository).save(any(WalletTransaction.class));
    }

    @Test
    void migratedWalletWithExistingTransactionsDoesNotReceiveDuplicateBonus() {
        Registration registration = userRegistration("user@example.com", "0.00");
        registration.setId(8L);
        WalletTransaction previousCredit = new WalletTransaction();
        previousCredit.setType("CREDIT");
        previousCredit.setAmount(new BigDecimal("100.00"));
        WalletTransaction previousDebit = new WalletTransaction();
        previousDebit.setType("DEBIT");
        previousDebit.setAmount(new BigDecimal("100.00"));
        when(registrationRepository.findByEmailForUpdate("user@example.com"))
                .thenReturn(Optional.of(registration));
        when(transactionRepository.findAllByRegistration_IdOrderByCreatedAtDesc(8L))
                .thenReturn(List.of(previousDebit, previousCredit));
        var summary = walletService.getWallet("user@example.com");
        assertEquals(new BigDecimal("0.00"), summary.balance());
        assertEquals(BigDecimal.ZERO, summary.signupCreditTotal());
        assertEquals(2, summary.totalTransactions());
        verify(transactionRepository, never()).save(any());
        verify(registrationRepository).save(registration);
    }

    private Registration userRegistration(String email, String balance) {
        Registration registration = new Registration();
        registration.setFullName("Passenger Name");
        registration.setEmail(email);
        registration.setPhone("555-0100");
        registration.setWalletBalance(new BigDecimal(balance));
        Login login = new Login();
        login.setEmail(email);
        login.setRole("USER");
        registration.setLogin(login);
        return registration;
    }
}
