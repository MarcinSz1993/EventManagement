package com.marcinsz.eventmanagementsystem.csv;

import com.opencsv.bean.CsvDate;
import jakarta.persistence.Column;
import lombok.Data;

import java.time.LocalDate;
@Data
public class UserCsvRepresentation {
    private String firstname;
    private String lastname;
    @Column(unique = true)
    private String email;
    private String username;
    @CsvDate(value = "yyyy-MM-dd")
    private LocalDate birthDate;
    @Column(unique = true)
    private String phoneNumber;
    @Column(unique = true)
    private String accountNumber;
}
