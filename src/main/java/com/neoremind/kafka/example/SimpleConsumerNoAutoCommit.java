package com.neoremind.kafka.example;

import com.google.common.io.Resources;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * High level API
 *
 * @author xu.zhang
 */
public class SimpleConsumerNoAutoCommit {

    public static void main(String[] args) throws IOException {
        // set up the consumer
        try (InputStream props = Resources.getResource("consumer.properties").openStream()) {
            Properties properties = new Properties();
            properties.load(props);
            properties.put("group.id", "my-replicated-test-2");
            properties.put("enable.auto.commit", "false");
            List<ConsumerRecord<String, String>> buffer = new ArrayList<>();
            final int minBatchSize = 5;
            try (KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(properties)) {
                kafkaConsumer.subscribe(Arrays.asList("my-replicated-topic"));
                while (true) {
                    ConsumerRecords<String, String> records = kafkaConsumer.poll(100);
                    for (ConsumerRecord<String, String> record : records) {
                        buffer.add(record);
                    }
                    if (buffer.size() >= minBatchSize) {
                        System.out.println(buffer);
                        kafkaConsumer.commitSync();
                        buffer.clear();
                    }
                }
            }
        }
    }
}
