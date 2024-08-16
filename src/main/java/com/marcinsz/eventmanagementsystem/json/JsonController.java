package com.marcinsz.eventmanagementsystem.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcinsz.eventmanagementsystem.request.CreateEventRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/json")
@RequiredArgsConstructor
public class JsonController {
    private final JsonService jsonService;
    private final ObjectMapper objectMapper;

    @GetMapping("/")
    public ResponseEntity<Resource> downloadUserPersonalJsonFile(@CookieValue String token) throws IOException {
        String jsonFileContent = jsonService.generateUserPersonalJsonFile(token);
        ByteArrayResource resource = new ByteArrayResource(jsonFileContent.getBytes(StandardCharsets.UTF_8));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=events.json")
                .contentType(MediaType.APPLICATION_JSON)
                .contentLength(jsonFileContent.length())
                .body(resource);
    }

    @PostMapping(value = "/",consumes = "multipart/form-data")
    public ResponseEntity<String> uploadEvents(@RequestParam("file") MultipartFile multipartFile,
                                               @CookieValue String token) throws IOException {
        List<CreateEventRequest> createEventRequestList = objectMapper.readValue(multipartFile.getInputStream(),
                objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, CreateEventRequest.class));
        return ResponseEntity.ok().body(jsonService.uploadEvents(createEventRequestList, token));
    }
}
