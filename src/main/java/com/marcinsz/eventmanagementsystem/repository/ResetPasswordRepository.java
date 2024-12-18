package com.marcinsz.eventmanagementsystem.repository;

import com.marcinsz.eventmanagementsystem.model.ResetPasswordToken;
import com.marcinsz.eventmanagementsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResetPasswordRepository extends JpaRepository<ResetPasswordToken,Long> {

    Optional<ResetPasswordToken> findByToken(String token);

    Optional<ResetPasswordToken> findByUser(User user);

}
