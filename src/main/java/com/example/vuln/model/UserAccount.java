package com.example.vuln.model;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Entity
@Table(
  name = "users",
  uniqueConstraints = {
    @UniqueConstraint(name="uk_users_username", columnNames = "username"),
    @UniqueConstraint(name="uk_users_email", columnNames = "email")
  }
)
public class UserAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(min=3, max=32)
    @Column(nullable = false, length = 32)
    private String username;

    @NotBlank @Email @Size(max=128)
    @Column(nullable = false, length = 128)
    private String email;

    @NotBlank
    @Column(nullable = false, length = 100) // เก็บ bcrypt hash ~60 ตัวอักษร
    private String passwordHash;

    // เพิ่ม field ที่อยากเก็บต่อได้ เช่น createdAt, roles ฯลฯ

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}
