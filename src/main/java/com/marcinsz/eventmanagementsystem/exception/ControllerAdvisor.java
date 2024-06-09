package com.marcinsz.eventmanagementsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.function.Consumer;

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
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(EventNotFoundException.class)
    public String EventNotFoundHandler(EventNotFoundException ex){
        return ex.getMessage();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.LOCKED)
    @ExceptionHandler(EventException.class)
    public String EventExceptionHandler(EventException ex){
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
        HashMap<String,String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors()
                .forEach(new Consumer<ObjectError>() {
                    @Override
                    public void accept(ObjectError error) {
                        String field = ((FieldError) error).getField();
                        String message = error.getDefaultMessage();
                        errors.put(field,message);
                    }
                });
        return new ResponseEntity<>(errors,HttpStatus.BAD_REQUEST);
    }
}
