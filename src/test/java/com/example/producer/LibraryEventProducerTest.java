package com.example.producer;

import com.example.domain.Book;
import com.example.domain.LibraryEvent;
import com.example.domain.LibraryEventType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import scala.Int;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LibraryEventProducerTest {

    @Mock
    KafkaTemplate<Integer, String> kafkaTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private LibraryEventProducer cut;

    @Test
    void sendLibraryEvent2_failure(TestInfo testInfo) {
        // given
        var book = Book.builder()
                .id(123)
                .author(testInfo.getDisplayName())
                .name(testInfo.getDisplayName())
                .build();
        var libraryEvent = LibraryEvent.builder()
                .id(null)
                .book(book)
                .build();
        var future = new CompletableFuture<SendResult<Integer, String>>();
        future.obtrudeException(new RuntimeException("Exception Calling Kafka"));

        // when
        Mockito.when(kafkaTemplate.send(Mockito.isA(ProducerRecord.class))).thenReturn(future);

        //then
        Assertions.assertThrows(Exception.class, () -> cut.sendLibraryEvent2(libraryEvent).get());
    }

    @Test
    void sendLibraryEvent2_success(TestInfo testInfo) throws JsonProcessingException, ExecutionException, InterruptedException {
        // given
        var book = Book.builder()
                .id(123)
                .author(testInfo.getDisplayName())
                .name(testInfo.getDisplayName())
                .build();
        var libraryEvent = LibraryEvent.builder()
                .id(null)
                .book(book)
                .eventType(LibraryEventType.NEW)
                .build();
        var future = new CompletableFuture<SendResult<Integer, String>>();

        var producerRecord = new ProducerRecord<>("library-events", libraryEvent.getId(), objectMapper.writeValueAsString(libraryEvent));
        var recordMetadata = new RecordMetadata(new TopicPartition("library-events", 1), 1, 342, System.currentTimeMillis(), 1, 2);
        SendResult<Integer, String> sendResult = new SendResult<>(producerRecord, recordMetadata);
        future.complete(sendResult);

        // when
        Mockito.when(kafkaTemplate.send(Mockito.isA(ProducerRecord.class))).thenReturn(future);

        //then
        var completableFuture = cut.sendLibraryEvent2(libraryEvent);

        var result = completableFuture.get();
        Assertions.assertEquals(result.getRecordMetadata().partition(), 1);
    }
}