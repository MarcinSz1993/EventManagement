package com.marcinsz.eventmanagementsystem.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.LinkedHashMap;

@ControllerAdvice
public class ControllerAdvisor {

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UserNotFoundException.class)
    public String UserNotFoundHandler(UserNotFoundException ex){
        return ex.getMessage();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(BadCredentialsException.class)
    public String BadCredentialsHandler(BadCredentialsException ex){
        return ex.getMessage();
    }

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<String> handleEventNotFoundException(EventNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.LOCKED)
    @ExceptionHandler(EventForecastTooEarlyException.class)
    public String EventExceptionHandler(EventForecastTooEarlyException ex){
        return ex.getMessage();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(NotYourEventException.class)
    public String NotYourEventExceptionHandler(NotYourEventException ex){
        return ex.getMessage();
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException ex){
        LinkedHashMap<String,String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getAllErrors()
                .forEach(error -> {
                    String field = ((FieldError) error).getField();
                    String message = error.getDefaultMessage();
                    errors.put(field,message);
                });
        return new ResponseEntity<>(errors,HttpStatus.BAD_REQUEST);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(WrongFileException.class)
    public String wrongFileExceptionHandler(Exception ex){
        return ex.getMessage();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    @ExceptionHandler(UserNotParticipantException.class)
    public String userNotParticipantExceptionHandler(Exception ex){
        return ex.getMessage();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ReviewAlreadyWrittenException.class)
    public String reviewAlreadyWrittenExceptionHandler(Exception ex){
        return ex.getMessage();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(EventNotFinishedException.class)
    public String eventNotFinishedExceptionHandler(Exception ex){
        return ex.getMessage();
    }
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    @ExceptionHandler(InvalidJsonFileException.class)
    public String invalidJsonFileExceptionHandler(Exception ex){
        return ex.getMessage();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(TicketAlreadyBoughtException.class)
    public String ticketAlreadyBoughtHandler(Exception ex){
        return ex.getMessage();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(BankServiceServerNotAvailableException.class)
    public String bankServiceServerNotAvailableHandler(Exception ex){
        return ex.getMessage();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(TransactionProcessServerException.class)
    public String transactionProcessServerHandler(Exception ex){
        return ex.getMessage();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.PROCESSING)
    @ExceptionHandler(TransactionProcessClientException.class)
    public String transactionProcessClientHandler(Exception ex){
        return ex.getMessage();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(EmptyCartException.class)
    public String emptyCartHandler(Exception ex){
        return ex.getMessage();
    }
    @ExceptionHandler(NotExistingEventInTheCart.class)
    public ResponseEntity<String> notExistingEventInTheCartHandler(Exception ex){
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsForBankServiceException.class)
    public ResponseEntity<String> badCredentialsForBankServiceHandler(Exception ex){
        return ResponseEntity.status(HttpStatus.LOCKED).body(ex.getMessage());
    }

    @ExceptionHandler(NotEnoughMoneyException.class)
    public ResponseEntity<String> notEnoughMoneyHandler(Exception ex){
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> httpMessageNotReadableExceptionHandler(){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request body cannot be null!");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> dataIntegrityViolationExceptionHandler(){
        return ResponseEntity.status(HttpStatus.CONFLICT).body("User with a data you provided already exists.");
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<String> userAlreadyExistsExceptionHandler(Exception ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(LocationNotFoundException.class)
    public ResponseEntity<String> locationNotFoundExceptionHandler(Exception ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(EventValidateException.class)
    public ResponseEntity<String> eventCompletedExceptionHandler(Exception ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<String> missingRequestCookieExceptionHandler(){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing required cookie.");
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<String>expiredJwtExceptionHandler(){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Expired JWT token. Please log in once again.");
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<String>malformedJwtExceptionHandler(Exception ex){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid JWT token." + ex.getMessage());
    }

    @ExceptionHandler(TokenException.class)
    public ResponseEntity<String>tokenExceptionHandler(Exception ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }
}


