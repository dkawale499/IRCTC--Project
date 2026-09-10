package com.sts.registration;

public record RegistrationRequest(String fullName, String email, String phone, String password) {
}
