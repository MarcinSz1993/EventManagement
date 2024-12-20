package com.marcinsz.eventmanagementsystem.service;


import com.marcinsz.eventmanagementsystem.exception.BadCredentialsException;
import com.marcinsz.eventmanagementsystem.exception.TokenException;
import com.marcinsz.eventmanagementsystem.exception.UserNotFoundException;
import com.marcinsz.eventmanagementsystem.model.ResetPasswordToken;
import com.marcinsz.eventmanagementsystem.model.Role;
import com.marcinsz.eventmanagementsystem.model.User;
import com.marcinsz.eventmanagementsystem.repository.ResetPasswordRepository;
import com.marcinsz.eventmanagementsystem.repository.UserRepository;
import com.marcinsz.eventmanagementsystem.request.ResetPasswordRequest;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@Slf4j
public class RestPasswordServiceTest {

    @Mock
    private ResetPasswordRepository resetPasswordRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private ResetPasswordService resetPasswordService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void resetPasswordShouldThrowTokenExceptionWhenTokenHasExpired() {
        String token = "reset-password-token";
        String newPassword = "new-password";
        String confirmPassword = "new-password";
        User user = createUser();
        ResetPasswordToken resetPasswordToken = createResetPasswordToken(user);
        resetPasswordToken.setExpireTime(LocalDateTime.now().minusMinutes(10));

        Mockito.when(resetPasswordRepository.findByToken(token)).thenReturn(Optional.of(resetPasswordToken));
        TokenException tokenException = Assertions.assertThrows(TokenException.class, () -> resetPasswordService.resetPassword(token, newPassword, confirmPassword));
        Assertions.assertEquals("Token has expired.", tokenException.getMessage());
    }

    @Test
    public void resetPasswordShouldThrowBadCredentialsExceptionWhenNewPasswordAndConfirmPasswordAreNotTheSame() {
        String token = "reset-password-token";
        String newPassword = "new-password";
        String confirmPassword = "not-the-same-new-password";
        User user = createUser();
        ResetPasswordToken resetPasswordToken = createResetPasswordToken(user);

        Mockito.when(resetPasswordRepository.findByToken(token)).thenReturn(Optional.of(resetPasswordToken));
        BadCredentialsException badCredentialsException = Assertions.assertThrows(BadCredentialsException.class, () -> resetPasswordService.resetPassword(token, newPassword, confirmPassword));
        Assertions.assertEquals(badCredentialsException.getMessage(), "Passwords are not the same!");
    }

    @Test
    public void resetPasswordShouldThrowTokenExceptionWithSpecifiedCommunicateWhenTokenDoesNotExist() {
        String notExistingToken = "not-existing-token";
        String newPassword = "new-password";
        String confirmPassword = "new-password";

        Mockito.when(resetPasswordRepository.findByToken(notExistingToken)).thenReturn(Optional.empty());
        TokenException tokenException = Assertions.assertThrows(TokenException.class, () -> resetPasswordService.resetPassword(notExistingToken, newPassword, confirmPassword));

        Assertions.assertEquals("Token not found.", tokenException.getMessage());

        Mockito.verify(resetPasswordRepository, Mockito.times(1)).findByToken(notExistingToken);
        Mockito.verify(passwordEncoder, Mockito.never()).encode(newPassword);
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
        Mockito.verify(resetPasswordRepository, Mockito.never()).save(Mockito.any(ResetPasswordToken.class));
    }

    @Test
    public void resetPasswordShouldChangePasswordAfterSuccessfullyResetPassword() {
        String token = "reset-password-token";
        String newPassword = "new-password";
        String confirmPassword = "new-password";
        String encodedPassword = "encoded-password";

        User user = createUser();
        ResetPasswordToken resetPasswordToken = createResetPasswordToken(user);

        Mockito.when(resetPasswordRepository.findByToken(token)).thenReturn(Optional.of(resetPasswordToken));
        Mockito.when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);

        resetPasswordService.resetPassword(token, newPassword, confirmPassword);

        Assertions.assertEquals(encodedPassword, user.getPassword());
        Mockito.verify(resetPasswordRepository, Mockito.times(1)).findByToken(token);
        Mockito.verify(passwordEncoder, Mockito.times(1)).encode(newPassword);
    }

    @Test
    public void resetPasswordShouldDeleteTokenAfterSuccessfullyResetPassword() {
        String token = "reset-password-token";
        String newPassword = "new-password";
        String confirmPassword = "new-password";
        String encodedPassword = "encoded-password";

        User user = createUser();
        ResetPasswordToken resetPasswordToken = createResetPasswordToken(user);

        Mockito.when(resetPasswordRepository.findByToken(token)).thenReturn(Optional.of(resetPasswordToken));
        Mockito.when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);

        ArgumentCaptor<ResetPasswordToken> tokenArgumentCaptor = ArgumentCaptor.forClass(ResetPasswordToken.class);
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);

        resetPasswordService.resetPassword(token, newPassword, confirmPassword);
        Mockito.verify(resetPasswordRepository).delete(tokenArgumentCaptor.capture());
        Mockito.verify(userRepository).save(userArgumentCaptor.capture());

        ResetPasswordToken tokenArgumentCaptorValue = tokenArgumentCaptor.getValue();
        User userArgumentCaptorValue = userArgumentCaptor.getValue();

        Assertions.assertEquals(resetPasswordToken.getToken(), tokenArgumentCaptorValue.getToken());
        Assertions.assertEquals(user, userArgumentCaptorValue);

        Mockito.verify(resetPasswordRepository, Mockito.times(1)).findByToken(token);
        Mockito.verify(passwordEncoder, Mockito.times(1)).encode(newPassword);
        Mockito.verify(userRepository, Mockito.times(1)).save(user);
    }

    @Test
    public void resetPasswordRequestShouldThrowIAEWithSpecifiedCommunicateWhenInputIsNull() {
        IllegalArgumentException illegalArgumentException = Assertions.assertThrows(IllegalArgumentException.class, () -> resetPasswordService.resetPasswordRequest(null));
        Assertions.assertEquals(illegalArgumentException.getMessage(), "resetPasswordRequest cannot be null");
    }

    @Test
    public void resetPasswordRequestShouldGenerateNewTokenEvenWhenTokenAlreadyExists2() throws MessagingException {
        //This test uses Mockito.spy
        ResetPasswordRequest resetPasswordRequest = createResetPasswordRequest();
        User user = createUser();
        String email = resetPasswordRequest.getEmail();
        ResetPasswordToken oldResetPasswordToken = createResetPasswordToken(user);
        ResetPasswordToken newResetPasswordToken = createResetPasswordToken(user);
        newResetPasswordToken.setId(2L);
        newResetPasswordToken.setToken("new-reset-password-token");

        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        Mockito.when(resetPasswordRepository.findByUser(user)).thenReturn(Optional.of(oldResetPasswordToken));
        Mockito.doNothing().when(notificationService).sendResetPasswordTokenToUser(Mockito.eq(email), Mockito.anyString());

        ResetPasswordService spiedResetPasswordService = Mockito.spy(new ResetPasswordService(resetPasswordRepository, userRepository, passwordEncoder, notificationService));
        Mockito.doReturn(newResetPasswordToken).when(spiedResetPasswordService).createPasswordResetToken(user);

        spiedResetPasswordService.resetPasswordRequest(resetPasswordRequest);

        Mockito.verify(resetPasswordRepository).save(newResetPasswordToken);
        Mockito.verify(userRepository, Mockito.times(1)).findByEmail(email);
        Mockito.verify(resetPasswordRepository, Mockito.times(1)).findByUser(user);
        Mockito.verify(resetPasswordRepository, Mockito.times(1)).delete(oldResetPasswordToken);
        Mockito.verify(resetPasswordRepository, Mockito.times(1)).flush();
        Mockito.verify(notificationService, Mockito.times(1)).sendResetPasswordTokenToUser(Mockito.eq(email), Mockito.anyString());
    }

    @Test
    public void resetPasswordRequestShouldGenerateNewTokenEvenWhenTokenAlreadyExists() throws MessagingException {
        //This test uses ArgumentCapture
        ResetPasswordRequest resetPasswordRequest = createResetPasswordRequest();
        User user = createUser();
        String email = resetPasswordRequest.getEmail();
        ResetPasswordToken oldResetPasswordToken = createResetPasswordToken(user);

        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        Mockito.when(resetPasswordRepository.findByUser(user)).thenReturn(Optional.of(oldResetPasswordToken));
        Mockito.doNothing().when(notificationService).sendResetPasswordTokenToUser(Mockito.eq(email), Mockito.anyString());

        ArgumentCaptor<ResetPasswordToken> tokenArgumentCaptor = ArgumentCaptor.forClass(ResetPasswordToken.class);
        resetPasswordService.resetPasswordRequest(resetPasswordRequest);
        Mockito.verify(resetPasswordRepository).save(tokenArgumentCaptor.capture());
        ResetPasswordToken capturedResetPasswordToken = tokenArgumentCaptor.getValue();

        log.info("Generated token: {}", capturedResetPasswordToken.getToken());
        Assertions.assertNotEquals(oldResetPasswordToken, capturedResetPasswordToken);


        Mockito.verify(userRepository, Mockito.times(1)).findByEmail(email);
        Mockito.verify(resetPasswordRepository, Mockito.times(1)).findByUser(user);
        Mockito.verify(resetPasswordRepository, Mockito.times(1)).delete(oldResetPasswordToken);
        Mockito.verify(resetPasswordRepository, Mockito.times(1)).flush();
        Mockito.verify(notificationService, Mockito.times(1)).sendResetPasswordTokenToUser(Mockito.eq(email), Mockito.anyString());
    }

    @Test
    public void resetPasswordRequestShouldRemoveExistingTokenAndFlushDatabaseWhenTokenAlreadyExists() throws MessagingException {
        ResetPasswordRequest resetPasswordRequest = createResetPasswordRequest();
        User user = createUser();
        ResetPasswordToken resetPasswordToken = createResetPasswordToken(user);

        Mockito.when(userRepository.findByEmail(resetPasswordRequest.getEmail())).thenReturn(Optional.of(user));
        Mockito.when(resetPasswordRepository.findByUser(user)).thenReturn(Optional.of(resetPasswordToken));

        resetPasswordService.resetPasswordRequest(resetPasswordRequest);

        Mockito.verify(userRepository, Mockito.times(1)).findByEmail(resetPasswordRequest.getEmail());
        Mockito.verify(resetPasswordRepository, Mockito.times(1)).delete(resetPasswordToken);
        Mockito.verify(resetPasswordRepository, Mockito.times(1)).flush();
    }

    @Test
    public void resetPasswordRequestShouldThrowUserNotFoundExceptionWhenUserIsNotFoundWithSpecifiedCommunicate() throws MessagingException {
        ResetPasswordRequest resetPasswordRequest = createResetPasswordRequest();
        String email = resetPasswordRequest.getEmail();

        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        UserNotFoundException userNotFoundException = Assertions.assertThrows(UserNotFoundException.class, () -> resetPasswordService.resetPasswordRequest(resetPasswordRequest));

        Assertions.assertEquals("There is no user with email: " + email, userNotFoundException.getMessage());

        Mockito.verify(userRepository, Mockito.times(1)).findByEmail(email);
        Mockito.verify(resetPasswordRepository, Mockito.never()).findByUser(Mockito.any(User.class));
        Mockito.verify(resetPasswordRepository, Mockito.never()).save(Mockito.any(ResetPasswordToken.class));
        Mockito.verify(notificationService, Mockito.never()).sendResetPasswordTokenToUser(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void resetPasswordRequestShouldCallDependenciesCorrectly() throws MessagingException {
        ResetPasswordRequest resetPasswordRequest = createResetPasswordRequest();
        String email = resetPasswordRequest.getEmail();
        User user = createUser();

        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        Mockito.when(resetPasswordRepository.findByUser(user)).thenReturn(Optional.empty());
        ResetPasswordToken resetPasswordToken = createResetPasswordToken(user);
        Mockito.doNothing().when(notificationService).sendResetPasswordTokenToUser(user.getEmail(), resetPasswordToken.getToken());

        resetPasswordService.resetPasswordRequest(resetPasswordRequest);

        Mockito.verify(userRepository, Mockito.times(1)).findByEmail(email);
        Mockito.verify(resetPasswordRepository, Mockito.times(1)).findByUser(user);
        Mockito.verify(resetPasswordRepository, Mockito.times(1)).save(Mockito.any(ResetPasswordToken.class));
        Mockito.verify(notificationService, Mockito.times(1)).sendResetPasswordTokenToUser(Mockito.eq(email), Mockito.anyString());
    }

    @Test
    public void resetPasswordRequestShouldGenerateToken2() throws MessagingException {
        //This test uses Mockito.Spy to mock a help method createPasswordResetToken.
        ResetPasswordRequest resetPasswordRequest = createResetPasswordRequest();
        String email = resetPasswordRequest.getEmail();
        User user = createUser();
        ResetPasswordToken expectedResetPasswordToken = createResetPasswordToken(user);

        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        Mockito.when(resetPasswordRepository.findByUser(user)).thenReturn(Optional.empty());
        Mockito.doNothing().when(notificationService).sendResetPasswordTokenToUser(Mockito.anyString(), Mockito.anyString());

        ResetPasswordService spiedResetPasswordService = Mockito.spy(new ResetPasswordService(
                resetPasswordRepository, userRepository, passwordEncoder, notificationService
        ));

        Mockito.doReturn(expectedResetPasswordToken).when(spiedResetPasswordService).createPasswordResetToken(user);
        ResetPasswordToken actualPasswordResetToken = spiedResetPasswordService.createPasswordResetToken(user);
        log.info("Mocked help method: {}", actualPasswordResetToken);

        spiedResetPasswordService.resetPasswordRequest(resetPasswordRequest);

        Assertions.assertNotNull(expectedResetPasswordToken.getToken());
        Assertions.assertEquals(expectedResetPasswordToken.getToken(), actualPasswordResetToken.getToken());
    }

    @Test
    public void resetPasswordRequestShouldGenerateValidTokenWithExactly36CharactersAnd9MinutesOfExpiringTime() throws MessagingException {
        //This test does not use Mockito.Spy to mock a help method createPasswordResetToken()
        ResetPasswordRequest resetPasswordRequest = createResetPasswordRequest();
        String email = resetPasswordRequest.getEmail();
        User user = createUser();

        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        ResetPasswordToken passwordResetToken = resetPasswordService.createPasswordResetToken(user);

        resetPasswordService.resetPasswordRequest(resetPasswordRequest);

        log.info("Generated token: {}", passwordResetToken.getToken());
        Assertions.assertNotNull(passwordResetToken.getToken());
        Assertions.assertEquals(9, Duration.between(LocalDateTime.now(), passwordResetToken.getExpireTime()).toMinutes());
        Assertions.assertEquals(36, passwordResetToken.getToken().length());
    }

    private ResetPasswordRequest createResetPasswordRequest() {
        return ResetPasswordRequest.builder()
                .email("test@test.com")
                .build();
    }

    private User createUser() {
        return User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .email("test@test.com")
                .username("johnny")
                .password("encodedPassword")
                .birthDate(LocalDate.of(1993, 4, 19))
                .role(Role.USER)
                .phoneNumber("123456789")
                .accountNumber("1234567890")
                .accountStatus("ACTIVE")
                .events(Collections.emptyList())
                .organizedEvents(Collections.emptyList())
                .build();
    }

    private ResetPasswordToken createResetPasswordToken(User user) {
        return ResetPasswordToken.builder()
                .id(1L)
                .token("reset-password-token")
                .expireTime(LocalDateTime.now().plusMinutes(10))
                .user(user)
                .build();
    }
}
