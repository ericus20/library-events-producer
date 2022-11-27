package com.example.controller;

import com.example.domain.Book;
import com.example.domain.LibraryEvent;
import com.example.producer.LibraryEventProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@AutoConfigureMockMvc
@WebMvcTest(LibraryEventsController.class)
class LibraryEventsControllerTest {

    @MockBean
    private LibraryEventProducer libraryEventProducer;

    @Autowired
    private MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void postLibraryEvent(TestInfo testInfo) throws Exception {
        //given
        var book = Book.builder()
                .id(123)
                .author(testInfo.getDisplayName())
                .name(testInfo.getDisplayName())
                .build();
        var libraryEvent = LibraryEvent.builder()
                .id(null)
                .book(book)
                .build();

        var json = objectMapper.writeValueAsString(libraryEvent);

        // Mockito.doNothing().when(libraryEventProducer).sendLibraryEvent2(Mockito.isA(LibraryEvent.class));
        Mockito.when(libraryEventProducer.sendLibraryEvent2(Mockito.isA(LibraryEvent.class))).thenReturn(null);

        //when
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/library-event")
                .content(json).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated());

    }

    @Test
    void postLibraryEvent4xx(TestInfo testInfo) throws Exception {
        //given
        var book = Book.builder()
                .id(null)
                .author(null)
                .name(testInfo.getDisplayName())
                .build();
        var libraryEvent = LibraryEvent.builder()
                .id(null)
                .book(book)
                .build();

        var json = objectMapper.writeValueAsString(libraryEvent);

        Mockito.doNothing().when(libraryEventProducer).sendLibraryEvent2(Mockito.isA(LibraryEvent.class));

        //when
        String expectedErrorMessage = "book.author - must not be blank,book.id - must not be null";
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/library-event")
                .content(json).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.content().string(expectedErrorMessage));

    }

}