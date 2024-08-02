package com.marcinsz.eventmanagementsystem.csv;

import com.opencsv.bean.CsvDate;
import lombok.Data;

import java.time.LocalDate;
@Data
public class UserCsvRepresentation {
    private String firstname;
    private String lastname;
    private String email;
    private String username;
    @CsvDate(value = "yyyy-MM-dd")
    private LocalDate birthDate;
    private String phoneNumber;
    private String accountNumber;
}
