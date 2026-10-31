package com.sts.wallet;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletTransactionView(
        Long id,
        String type,
        String description,
        LocalDateTime createdAt,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String status) {
}
