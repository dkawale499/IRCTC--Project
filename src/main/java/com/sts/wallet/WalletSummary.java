package com.sts.wallet;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record WalletSummary(
        BigDecimal balance,
        BigDecimal signupCreditTotal,
        long totalTransactions,
        String passengerName,
        String email,
        String phone,
        LocalDateTime registeredAt,
        List<WalletTransactionView> transactions) {
}
