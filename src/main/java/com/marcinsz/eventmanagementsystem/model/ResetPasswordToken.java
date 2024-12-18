package com.marcinsz.eventmanagementsystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Table(name = "reset_token")
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    private LocalDateTime expireTime;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
