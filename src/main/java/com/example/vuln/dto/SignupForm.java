package com.example.vuln.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class SignupForm {
    @NotBlank @Size(min=3, max=32)
    private String username;

    @NotBlank @Email @Size(max=128)
    private String email;

    @NotBlank @Size(min=6, max=72) // BCrypt แนะนำ <=72
    private String password;

    // getters/setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
