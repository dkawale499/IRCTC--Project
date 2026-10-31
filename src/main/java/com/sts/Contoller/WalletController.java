package com.sts.Contoller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.sts.service.WalletService;
import com.sts.wallet.WalletSummary;

@RestController
@RequestMapping("/wallet")
@PreAuthorize("hasRole('USER')")
public class WalletController {
    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    public WalletSummary getWallet(@AuthenticationPrincipal String email) {
        return walletService.getWallet(email);
    }
}
