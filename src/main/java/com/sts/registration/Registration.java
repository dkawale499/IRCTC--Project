package com.sts.registration;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.sts.login.Login;
import com.sts.wallet.WalletTransaction;

@Entity
@Table(name = "registrations")
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private BigDecimal walletBalance = BigDecimal.ZERO;
    private boolean walletInitialized;
    private LocalDateTime registeredAt;

    @OneToOne(mappedBy = "registration", cascade = CascadeType.ALL, orphanRemoval = true)
    private Login login;

    @OneToMany(mappedBy = "registration", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WalletTransaction> walletTransactions = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public BigDecimal getWalletBalance() { return walletBalance == null ? BigDecimal.ZERO : walletBalance; }
    public void setWalletBalance(BigDecimal walletBalance) { this.walletBalance = walletBalance; }
    public boolean isWalletInitialized() { return walletInitialized; }
    public void setWalletInitialized(boolean walletInitialized) { this.walletInitialized = walletInitialized; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
    public Login getLogin() { return login; }
    public void setLogin(Login login) { this.login = login; }
    public List<WalletTransaction> getWalletTransactions() { return walletTransactions; }
    public void setWalletTransactions(List<WalletTransaction> walletTransactions) { this.walletTransactions = walletTransactions; }
}
