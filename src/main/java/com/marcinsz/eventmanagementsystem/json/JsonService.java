package com.marcinsz.eventmanagementsystem.json;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.exception.InvalidJsonFileException;
import com.marcinsz.eventmanagementsystem.exception.UserNotFoundException;
import com.marcinsz.eventmanagementsystem.mapper.EventMapper;
import com.marcinsz.eventmanagementsystem.model.Event;
import com.marcinsz.eventmanagementsystem.model.User;
import com.marcinsz.eventmanagementsystem.repository.EventRepository;
import com.marcinsz.eventmanagementsystem.repository.UserRepository;
import com.marcinsz.eventmanagementsystem.request.CreateEventRequest;
import com.marcinsz.eventmanagementsystem.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@Slf4j
@Validated
@RequiredArgsConstructor
public class JsonService {
    private final ObjectMapper objectMapper;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public String generateUserPersonalJsonFile(String token) throws JsonProcessingException {
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
        List<Event> userEvents = user.getEvents();
        List<EventDto> userEventsDto = EventMapper.convertListEventToListEventDto(userEvents);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return objectMapper.writeValueAsString(userEventsDto);
    }

    @Transactional
    public String uploadEvents(@Valid List<CreateEventRequest> eventRequestList,String token) throws IOException {
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));

        Schema schema = loadSchemaStream();
        validateData(eventRequestList, schema);
        List<Event> eventList = prepareEvents(eventRequestList, user);
        eventRepository.saveAll(eventList);
        return "Added " + eventList.size() + " events";
    }

    private static List<Event> prepareEvents(List<CreateEventRequest> createEventRequestList, User user) {
        List<Event> eventList = createEventRequestList.stream()
                .map(EventMapper::convertCreateEventRequestToEvent)
                .toList();
        for (Event event : eventList) {
            event.setOrganizer(user);
        }
        return eventList;
    }

    private void validateData(List<CreateEventRequest> createEventRequestList, Schema schema) throws JsonProcessingException {
        try {
            for (CreateEventRequest createEventRequest : createEventRequestList) {
                String jsonData = objectMapper.writeValueAsString(createEventRequest);
                JSONObject jsonObject = new JSONObject(jsonData);
                schema.validate(jsonObject);
            }
            log.info("Json file is valid.");
        } catch (ValidationException e) {
            log.error("Json file you uploaded is not valid.");
            throw new InvalidJsonFileException();
        }
    }

    private Schema loadSchemaStream() throws IOException {
        try (InputStream schemaStream = JsonService.class.getResourceAsStream("/jsonschema/schema-createeventrequest.json")) {
            if (schemaStream == null) {
                throw new JsonParseException("schema-createeventrequest.json not found");
            }
            JSONObject rawSchema = new JSONObject(new JSONTokener(schemaStream));
            return SchemaLoader.load(rawSchema);
        } catch (Exception exception) {
            throw new IOException(exception);
        }
    }
// Below commented method I created is to generate json schema. The method is universal
    // that's why I did not remove it. It can be helpful on the future.
//    public void generateAndSaveJsonSchema(Class<?> classToSchema) throws IOException {
//        JsonSchemaGenerator jsonSchemaGenerator = new JsonSchemaGenerator(objectMapper);
//        JsonSchema jsonSchema = jsonSchemaGenerator.generateSchema(classToSchema);
//        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File("schema-" + classToSchema.getSimpleName().toLowerCase() + ".json"), jsonSchema);
//        System.out.println("Json schema for class " + classToSchema.getSimpleName() + " has been generated and saved.");
//    }
}

