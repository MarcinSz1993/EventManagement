package com.marcinsz.eventmanagementsystem.csv;

import com.marcinsz.eventmanagementsystem.exception.WrongFileException;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class UserCsvHandler implements CsvHeadersValidator, CsvRecordsValidator<UserCsvRepresentation>, CsvFileParser<UserCsvRepresentation> {
    @Override
    public void validateHeaders(String[] csvHeadersFromFile) throws WrongFileException {
        List<String> acceptedHeaders = List.of("firstname", "lastname", "email", "username", "birthDate", "phoneNumber", "accountNumber");
        if (csvHeadersFromFile == null || !acceptedHeaders.equals(List.of(csvHeadersFromFile))) {
            throw new WrongFileException("Incorrect headers in the file!");
        }
    }

    @Override
    public void validateRecords(UserCsvRepresentation record) throws WrongFileException {
        if (record.getFirstname() == null || record.getFirstname().isEmpty()) {
            throw new WrongFileException("First name is missing or empty.");
        }
        if (record.getLastname() == null || record.getLastname().isEmpty()) {
            throw new WrongFileException("Last name is missing or empty");
        }
        if (record.getEmail() == null || record.getEmail().isEmpty()) {
            throw new WrongFileException("Email is missing or empty");
        }
        if (record.getUsername() == null || record.getUsername().isEmpty()) {
            throw new WrongFileException("Username is missing or empty");
        }
        if (record.getBirthDate() == null || record.getBirthDate().isAfter(LocalDate.now())) {
            throw new WrongFileException("Birth date is missing or later than current date");
        }
        if (record.getPhoneNumber() == null || record.getPhoneNumber().isEmpty()) {
            throw new WrongFileException("Phone number is missing or empty");
        }
        if (record.getAccountNumber() == null || record.getAccountNumber().isEmpty()) {
            throw new WrongFileException("Account number is missing or empty");
        }
    }

    @Override
    public Set<UserCsvRepresentation> parse(Reader reader, MultipartFile file) throws IOException, CsvValidationException {
        try (CSVReader csvReader = new CSVReader(reader)) {
            String[] headersFromFile = csvReader.readNext();
            validateHeaders(headersFromFile);
            csvReader.close();

            HeaderColumnNameMappingStrategy<UserCsvRepresentation> strategy = new HeaderColumnNameMappingStrategy<>();
            strategy.setType(UserCsvRepresentation.class);
            try (Reader newReader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                CsvToBean<UserCsvRepresentation> csvToBean = new CsvToBeanBuilder<UserCsvRepresentation>(newReader)
                        .withMappingStrategy(strategy)
                        .withIgnoreEmptyLine(true)
                        .withIgnoreLeadingWhiteSpace(true)
                        .build();

                List<UserCsvRepresentation> userCsvRepresentationList = csvToBean.parse();
                for (UserCsvRepresentation record : userCsvRepresentationList) {
                    validateRecords(record);
                }
                return new HashSet<>(userCsvRepresentationList);
            }
        }
    }
}
