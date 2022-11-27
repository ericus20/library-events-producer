package com.example.producer;

import com.example.domain.LibraryEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionException;

@Slf4j
@Component
@RequiredArgsConstructor
public class LibraryEventProducer {

    public static final String TOPIC = "library-events";
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<Integer, String> kafkaTemplate;

    public void sendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {
        var key = libraryEvent.getId();
        var value = objectMapper.writeValueAsString(libraryEvent);

        var completableFuture = kafkaTemplate.sendDefault(key, value);
        completableFuture.whenComplete((result, throwable) -> {
            if (Objects.nonNull(throwable)) {
                handleFailure(key, value, throwable);
            } else {
                // handle success with (result)
                handleSuccess(key, value, result);
            }
        });
    }

    public void sendLibraryEvent2(LibraryEvent libraryEvent) throws JsonProcessingException {
        var key = libraryEvent.getId();
        var value = objectMapper.writeValueAsString(libraryEvent);

        ProducerRecord<Integer, String> producerRecord = buildProducerRecord(key, value, TOPIC);
        var completableFuture = kafkaTemplate.send(producerRecord);
        completableFuture.whenComplete((result, throwable) -> {
            if (Objects.nonNull(throwable)) {
                handleFailure(key, value, throwable);
            } else {
                // handle success with (result)
                handleSuccess(key, value, result);
            }
        });
    }

    private ProducerRecord<Integer, String> buildProducerRecord(Integer key, String value, String topic) {

        List<Header> recordHeaders = List.of(new RecordHeader("event-source", "scanner".getBytes()));
        return new ProducerRecord<>(topic, null, key, value, recordHeaders);
    }

    private void handleFailure(Integer key, String value, Throwable throwable) {
        try {
            if (throwable instanceof CompletionException && Objects.nonNull(throwable.getCause())) {
                // handle failure with throwable.getCause()
                log.error("Error sending the Message", throwable.getCause());
            } else {
                log.error("Error sending the Message and the exception is {}", throwable.getMessage());
            }
        } catch (Throwable ex) {
            log.error("Error in OnFailure: {}", throwable.getMessage());
        }
    }

    private void handleSuccess(Integer key, String message, SendResult<Integer, String> result) {
        log.info("Message Sent Successfully for the key: {} and the value is {} and partition is {}", key, message, result.getRecordMetadata().partition());
    }

}
