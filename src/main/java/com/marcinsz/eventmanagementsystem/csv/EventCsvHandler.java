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
public class EventCsvHandler implements CsvHeadersValidator, CsvRecordsValidator<EventCsvRepresentation>, CsvFileParser<EventCsvRepresentation> {
    @Override
    public void validateHeaders(String[] csvHeadersFromFile) throws WrongFileException {
        List<String> acceptedHeaders = List.of("eventname", "eventdescription", "location", "maxattendees", "eventdate", "ticketprice", "eventtarget");
        if (csvHeadersFromFile == null || !acceptedHeaders.equals(List.of(csvHeadersFromFile))) {
            throw new WrongFileException("Incorrect headers in the file!");
        }
    }

    @Override
    public void validateRecords(EventCsvRepresentation record) throws WrongFileException {
        if (record.getEventName() == null || record.getEventName().isEmpty()) {
            throw new WrongFileException("Event name is missing or empty");
        }
        if (record.getEventDescription() == null || record.getEventDescription().isEmpty()) {
            throw new WrongFileException("Event description is missing or empty");
        }
        if (record.getLocation() == null || record.getLocation().isEmpty()) {
            throw new WrongFileException("Location is missing or empty");
        }
        if (record.getMaxAttendees() == 0) {
            throw new WrongFileException("Max attendees is not correct value");
        }
        if (record.getEventDate() == null || record.getEventDate().isBefore(LocalDate.now())) {
            throw new WrongFileException("Event date is missing or earlier than current date");
        }
        if (record.getEventTarget() == null) {
            throw new WrongFileException("Event target is missing");
        }
    }

    @Override
    public Set<EventCsvRepresentation> parse(Reader reader, MultipartFile file) throws IOException, CsvValidationException {
        try (CSVReader csvReader = new CSVReader(reader)) {
            String[] headersFromFile = csvReader.readNext();
            validateHeaders(headersFromFile);
            csvReader.close();

            HeaderColumnNameMappingStrategy<EventCsvRepresentation> strategy =
                    new HeaderColumnNameMappingStrategy<>();
            strategy.setType(EventCsvRepresentation.class);

            try (Reader newReader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                CsvToBean<EventCsvRepresentation> csvToBean = new CsvToBeanBuilder<EventCsvRepresentation>(newReader)
                        .withIgnoreLeadingWhiteSpace(true)
                        .withIgnoreEmptyLine(true)
                        .withMappingStrategy(strategy)
                        .build();
                List<EventCsvRepresentation> csvRepresentationList = csvToBean.parse();
                for (EventCsvRepresentation record : csvRepresentationList) {
                    validateRecords(record);
                }
                return new HashSet<>(csvRepresentationList);
            }
        }
    }
}

