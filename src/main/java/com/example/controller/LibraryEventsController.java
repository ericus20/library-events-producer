package com.example.controller;

import com.example.domain.LibraryEvent;
import com.example.domain.LibraryEventType;
import com.example.producer.LibraryEventProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/library-event")
@RequiredArgsConstructor
public class LibraryEventsController {

    private final LibraryEventProducer libraryEventProducer;

    @PostMapping
    public ResponseEntity<LibraryEvent> postLibraryEvent(@RequestBody @Valid LibraryEvent libraryEvent) throws JsonProcessingException {


        libraryEvent.setEventType(LibraryEventType.NEW);
        libraryEventProducer.sendLibraryEvent2(libraryEvent);
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }
}
