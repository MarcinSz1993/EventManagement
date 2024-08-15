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
    @NotEmpty
    @NotBlank
    private String eventName;
    @Min(1)
    @Max(5)
    private int degree;
    @NotBlank
    private String content;
}
