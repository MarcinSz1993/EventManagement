package com.marcinsz.eventmanagementsystem.controller;

import com.marcinsz.eventmanagementsystem.request.ResetPasswordRequest;
import com.marcinsz.eventmanagementsystem.service.ResetPasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/password")
@RequiredArgsConstructor
public class ResetPasswordController {

    private final ResetPasswordService resetPasswordService;

    @PostMapping("/request")
    public void resetPasswordRequest(@RequestBody @Valid ResetPasswordRequest resetPasswordRequest) throws Throwable {
        resetPasswordService.resetPasswordRequest(resetPasswordRequest);
    }

    @PostMapping("/reset")
    public void resetPassword(@RequestParam String token,@RequestParam String newPassword,@RequestParam String confirmNewPassword) {
        resetPasswordService.resetPassword(token,newPassword,confirmNewPassword);
    }

}
