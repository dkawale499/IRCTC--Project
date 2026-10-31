package com.sts.binding;

import java.util.Arrays;
import java.util.Optional;

public enum TicketStatus {
    CONFIRMED("Confirmed"),
    RAC("RAC"),
    WAITLISTED("Waitlisted"),
    CANCELLED("Cancelled");

    private final String displayName;

    TicketStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean matches(String value) {
        return value != null && displayName.equalsIgnoreCase(value.trim());
    }

    public static Optional<TicketStatus> from(String value) {
        return Arrays.stream(values()).filter(status -> status.matches(value)).findFirst();
    }
}
