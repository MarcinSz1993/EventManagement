package com.marcinsz.eventmanagementsystem.csv;

import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/csv")
@RequiredArgsConstructor
public class CsvController {

    private final CsvService csvService;

    @PostMapping(value = "/uploadUsers", consumes = {"multipart/form-data"})
    public ResponseEntity<Integer> uploadUsers(
            @RequestPart("file") MultipartFile file
    ) throws IOException, CsvException {
        return ResponseEntity.ok().body(csvService.uploadUsers(file));
    }

    @PostMapping(value = "/uploadEvents", consumes = {"multipart/form-data"})
    public ResponseEntity<String> uploadEvents(
            @RequestPart("file") MultipartFile file,
            @CookieValue String token
    ) throws IOException, CsvValidationException {
        return ResponseEntity.ok().body(csvService.uploadEvents(file, token));
    }
}

