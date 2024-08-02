package com.marcinsz.eventmanagementsystem.csv;

import com.marcinsz.eventmanagementsystem.exception.UserNotFoundException;
import com.marcinsz.eventmanagementsystem.exception.WrongFileException;
import com.marcinsz.eventmanagementsystem.mapper.EventMapper;
import com.marcinsz.eventmanagementsystem.mapper.UserMapper;
import com.marcinsz.eventmanagementsystem.model.Event;
import com.marcinsz.eventmanagementsystem.model.User;
import com.marcinsz.eventmanagementsystem.repository.EventRepository;
import com.marcinsz.eventmanagementsystem.repository.UserRepository;
import com.marcinsz.eventmanagementsystem.service.JwtService;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CsvService {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public Integer uploadUsers(MultipartFile file) throws IOException, CsvValidationException {
        Set<User> users = parseCsvUsers(file);
        userRepository.saveAll(users);
        return users.size();
    }

    public String uploadEvents(MultipartFile file, String token) throws IOException, CsvValidationException {
        Set<Event> events = parseCsvEvents(file, token);
        eventRepository.saveAll(events);
        return "Added " + events.size() + " events";
    }


    public Set<Event> parseCsvEvents(MultipartFile file, String token) throws IOException, CsvValidationException {
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CSVReader csvReader = new CSVReader(reader);
            String[] headersFromFile = csvReader.readNext();
            validateEventCsvFileHeaders(headersFromFile);
            reader.close();

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
                    validateEventCsvRecord(record);
                }
                return csvRepresentationList
                        .stream()
                        .map(eventCsvRepresentation -> {
                            Event event = EventMapper.convertEventCsvRepresentationToEvent(eventCsvRepresentation);
                            event.setOrganizer(user);
                            return event;
                        })
                        .collect(Collectors.toSet());
            }
        }
    }

    private Set<User> parseCsvUsers(MultipartFile file) throws IOException, CsvValidationException {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CSVReader csvReader = new CSVReader(reader);
            String[] headersFromFile = csvReader.readNext();
            validateUserCsvFileHeaders(headersFromFile);
            reader.close();

            HeaderColumnNameMappingStrategy<UserCsvRepresentation> strategy =
                    new HeaderColumnNameMappingStrategy<>();
            strategy.setType(UserCsvRepresentation.class);
            try(Reader newReader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                CsvToBean<UserCsvRepresentation> csvToBean = new CsvToBeanBuilder<UserCsvRepresentation>(newReader)
                        .withMappingStrategy(strategy)
                        .withIgnoreEmptyLine(true)
                        .withIgnoreLeadingWhiteSpace(true)
                        .build();
                List<UserCsvRepresentation> userCsvRepresentationList = csvToBean.parse();
                for (UserCsvRepresentation record : userCsvRepresentationList) {
                    validateUserCsvFileRecord(record);
                }
                return userCsvRepresentationList
                        .stream()
                        .map(userCsvRepresentation -> {
                            User user = UserMapper.convertUserCsvRepresentationToUser(userCsvRepresentation);
                            user.setPassword(passwordEncoder.encode(user.getPassword()));
                            return user;
                        }).collect(Collectors.toSet());
            }
        }
    }

    private void validateUserCsvFileHeaders(String[] headersFromFile) {
        List<String> acceptedHeaders = List.of("firstname","lastname","email","username","birthDate","phoneNumber","accountNumber");
        if(headersFromFile == null || !acceptedHeaders.equals(List.of(headersFromFile))){
            throw new WrongFileException("Incorrect headers in the file!");
        }
    }

    private void validateEventCsvFileHeaders(String[] headersFromFile) {
        List<String> acceptedHeaders = List.of("eventname","eventdescription","location","maxattendees","eventdate","ticketprice","eventtarget");
        if(headersFromFile == null || !acceptedHeaders.equals(List.of(headersFromFile))){
            throw new WrongFileException("Incorrect headers in the file!");
        }
    }

    private void validateUserCsvFileRecord(UserCsvRepresentation record) {
        if(record.getFirstname() == null || record.getFirstname().isEmpty()){
            throw new WrongFileException("First name is missing or empty.");
        }
        if(record.getLastname() == null || record.getLastname().isEmpty()){
            throw new WrongFileException("Last name is missing or empty");
        }
        if(record.getEmail() == null || record.getEmail().isEmpty()){
            throw new WrongFileException("Email is missing or empty");
        }
        if(record.getUsername() == null || record.getUsername().isEmpty()){
            throw new WrongFileException("Username is missing or empty");
        }
        if(record.getBirthDate() == null || record.getBirthDate().isAfter(LocalDate.now())){
            throw new WrongFileException("Birth date is missing or later than current date");
        }
        if(record.getPhoneNumber() == null || record.getPhoneNumber().isEmpty()){
            throw new WrongFileException("Phone number is missing or empty");
        }
        if(record.getAccountNumber() == null || record.getAccountNumber().isEmpty()){
            throw new WrongFileException("Account number is missing or empty");
        }
    }

    private void validateEventCsvRecord(EventCsvRepresentation record) {

        if (record.getEventName() == null || record.getEventName().isEmpty()){
            throw new WrongFileException("Event name is missing or empty");
        }
        if (record.getEventDescription() == null || record.getEventDescription().isEmpty()){
            throw new WrongFileException("Event description is missing or empty");
        }
        if (record.getLocation() == null || record.getLocation().isEmpty()){
            throw new WrongFileException("Location is missing or empty");
        }
        if (record.getMaxAttendees() == 0){
            throw new WrongFileException("Max attendees is not correct value");
        }
        if (record.getEventDate() == null || record.getEventDate().isBefore(LocalDate.now())){
            throw new WrongFileException("Event date is missing or earlier than current date");
        }
        if (record.getEventTarget() == null){
            throw new WrongFileException("Event target is missing");
        }
    }


}
