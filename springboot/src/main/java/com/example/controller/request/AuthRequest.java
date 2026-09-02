package com.example.controller.request;

import com.example.entity.Account;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequest(
        @NotBlank @Size(max = 64) String username,
        @NotBlank @Size(max = 72) String password,
        @NotBlank String role) {

    public Account toAccount() {
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(password);
        account.setRole(role);
        return account;
    }
}
