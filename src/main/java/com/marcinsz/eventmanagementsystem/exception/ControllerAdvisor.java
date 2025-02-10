package com.marcinsz.eventmanagementsystem.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ControllerAdvisor {

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String,String>> illegalStateExceptionHandler(IllegalStateException ex){
        Map<String,String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getLocalizedMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String,String>> UserNotFoundHandler(UserNotFoundException ex) {
        Map<String,String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> BadCredentialsHandler(BadCredentialsException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEventNotFoundException(EventNotFoundException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }


    @ResponseBody
    @ResponseStatus(HttpStatus.LOCKED)
    @ExceptionHandler(EventForecastTooEarlyException.class)
    public Map<String, String> EventExceptionHandler(EventForecastTooEarlyException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return errorResponse;
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(NotYourEventException.class)
    public String NotYourEventExceptionHandler(NotYourEventException ex) {
        return ex.getMessage();
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<Map<String, String>> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(WrongFileException.class)
    public String wrongFileExceptionHandler(Exception ex) {
        return ex.getMessage();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    @ExceptionHandler(UserNotParticipantException.class)
    public ResponseEntity<Map<String,String>> userNotParticipantExceptionHandler(Exception ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(errorResponse);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ReviewAlreadyWrittenException.class)
    public ResponseEntity<Map<String,String>> reviewAlreadyWrittenExceptionHandler(Exception ex) {
        Map<String,String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(EventNotFinishedException.class)
    public ResponseEntity<Map<String,String>> eventNotFinishedExceptionHandler(Exception ex) {
        Map<String,String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    @ExceptionHandler(InvalidJsonFileException.class)
    public String invalidJsonFileExceptionHandler(Exception ex) {
        return ex.getMessage();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(TicketAlreadyBoughtException.class)
    public String ticketAlreadyBoughtHandler(Exception ex) {
        return ex.getMessage();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(BankServiceServerNotAvailableException.class)
    public String bankServiceServerNotAvailableHandler(Exception ex) {
        return ex.getMessage();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(TransactionProcessServerException.class)
    public String transactionProcessServerHandler(Exception ex) {
        return ex.getMessage();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.PROCESSING)
    @ExceptionHandler(TransactionProcessClientException.class)
    public String transactionProcessClientHandler(Exception ex) {
        return ex.getMessage();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(EmptyCartException.class)
    public String emptyCartHandler(Exception ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(NotExistingEventInTheCart.class)
    public ResponseEntity<String> notExistingEventInTheCartHandler(Exception ex) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsForBankServiceException.class)
    public ResponseEntity<String> badCredentialsForBankServiceHandler(Exception ex) {
        return ResponseEntity.status(HttpStatus.LOCKED).body(ex.getMessage());
    }

    @ExceptionHandler(NotEnoughMoneyException.class)
    public ResponseEntity<String> notEnoughMoneyHandler(Exception ex) {
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String,String>> httpMessageNotReadableExceptionHandler(Exception ex) {
        Map<String,String> errorResponse = new HashMap<>();
        errorResponse.put("message",ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> dataIntegrityViolationExceptionHandler() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Event with a data you provided already exists.");
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String,String>> userAlreadyExistsExceptionHandler(Exception ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(LocationNotFoundException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String,String>> locationNotFoundExceptionHandler(Exception ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ResponseBody
    @ExceptionHandler(EventValidateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> eventCompletedExceptionHandler(Exception ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity
                .badRequest()
                .body(errorResponse);
    }

    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<String> missingRequestCookieExceptionHandler() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing required cookie.");
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<String> expiredJwtExceptionHandler() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Expired JWT token. Please log in once again.");
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<String> malformedJwtExceptionHandler(Exception ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid JWT token." + ex.getMessage());
    }

    @ExceptionHandler(TokenException.class)
    public ResponseEntity<String> tokenExceptionHandler(Exception ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<Map<String,String>> dateTimeParseExceptionHandler() {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", "Incorrect date-birth format. Should be YYYY-MM-DD format.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}


