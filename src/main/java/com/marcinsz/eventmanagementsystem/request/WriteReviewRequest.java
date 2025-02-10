package com.marcinsz.eventmanagementsystem.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WriteReviewRequest {
    @NotEmpty(message = "Event name cannot be empty.")
    @NotBlank(message = "Event name cannot be empty.")
    private String eventName;
    @Min(value = 1,message = "Choose the degree between 1 and 5.")
    @Max(value = 5,message = "Choose the degree between 1 and 5.")
    private int degree;
    @NotBlank(message = "Review content cannot be empty.")
    private String content;
}
