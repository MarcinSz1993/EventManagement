package com.marcinsz.eventmanagementsystem.controller;

import com.marcinsz.eventmanagementsystem.request.ResetPasswordRequest;
import com.marcinsz.eventmanagementsystem.service.ResetPasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password")
@RequiredArgsConstructor
public class ResetPasswordController {

    private final ResetPasswordService resetPasswordService;

    @PostMapping("/request")
    public void resetPasswordRequest(@RequestBody ResetPasswordRequest resetPasswordRequest) throws Throwable {
        resetPasswordService.resetPasswordRequest(resetPasswordRequest);
    }

    @PostMapping("/reset")
    public void resetPassword(@RequestParam String token,@RequestParam String newPassword,@RequestParam String confirmNewPassword) {
        resetPasswordService.resetPassword(token,newPassword,confirmNewPassword);
    }

}
