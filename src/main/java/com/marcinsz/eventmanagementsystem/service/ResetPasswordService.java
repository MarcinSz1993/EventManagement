package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.exception.BadCredentialsException;
import com.marcinsz.eventmanagementsystem.exception.TokenException;
import com.marcinsz.eventmanagementsystem.exception.UserNotFoundException;
import com.marcinsz.eventmanagementsystem.model.ResetPasswordToken;
import com.marcinsz.eventmanagementsystem.model.User;
import com.marcinsz.eventmanagementsystem.repository.ResetPasswordRepository;
import com.marcinsz.eventmanagementsystem.repository.UserRepository;
import com.marcinsz.eventmanagementsystem.request.ResetPasswordRequest;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResetPasswordService {

    private final ResetPasswordRepository resetPasswordRepository;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final NotificationService notificationService;

    @Transactional
    public void resetPasswordRequest(ResetPasswordRequest resetPasswordRequest) throws MessagingException {

        User user = userRepository.findByEmail(resetPasswordRequest.getEmail()).orElseThrow(() -> UserNotFoundException.forEmail(resetPasswordRequest.getEmail()));

        resetPasswordRepository.findByUser(user).ifPresent(resetPasswordToken -> {
             resetPasswordRepository.delete(resetPasswordToken);
             resetPasswordRepository.flush();
         });

        ResetPasswordToken passwordResetToken = createPasswordResetToken(resetPasswordRequest.getEmail());
        resetPasswordRepository.save(passwordResetToken);

        sendEmailToUser(resetPasswordRequest.getEmail(), passwordResetToken.getToken());
    }

    public void resetPassword(String token,String newPassword,String confirmNewPassword){
        ResetPasswordToken resetPasswordToken = resetPasswordRepository.findByToken(token).orElseThrow(() -> new TokenException("Token not found."));

        validateResetPasswordInput(token, newPassword, confirmNewPassword, resetPasswordToken);

        User user = resetPasswordToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetPasswordRepository.delete(resetPasswordToken);
    }

    private void validateResetPasswordInput(String token, String newPassword, String confirmNewPassword, ResetPasswordToken resetPasswordToken) {
        if (!newPassword.equals(confirmNewPassword)){
            throw new BadCredentialsException("Passwords are not the same!");
        }
        if (!resetPasswordToken.getToken().equals(token)) {
            throw new TokenException("Invalid token");
        }
        if (resetPasswordToken.getExpireTime().isBefore(LocalDateTime.now())){
            throw new TokenException("Token has expired.");
        }
    }

    private void sendEmailToUser(String email,String token) throws MessagingException {
        notificationService.sendResetPasswordTokenToUser(email,token);
    }

    private ResetPasswordToken createPasswordResetToken(String email){
        User user = userRepository.findByEmail(email).orElseThrow(() -> UserNotFoundException.forEmail(email));
        String token = UUID.randomUUID().toString();

        return ResetPasswordToken.builder()
                .token(token)
                .expireTime(LocalDateTime.now().plusMinutes(10))
                .user(user)
                .build();
    }
}
