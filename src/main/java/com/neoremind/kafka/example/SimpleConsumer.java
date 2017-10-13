package com.neoremind.kafka.example;

import com.google.common.io.Resources;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

/**
 * High level API
 * <p>
 * https://kafka.apache.org/0100/javadoc/index.html?org/apache/kafka/clients/consumer/KafkaConsumer.html
 * <p>
 * 新提供的New Consumer API，对以前旧版本中几种API实现的功能进行了整合与统一实现。
 *
 * @author xu.zhang
 */
public class SimpleConsumer {

    public static void main(String[] args) throws IOException {
        // set up the consumer
        try (InputStream props = Resources.getResource("consumer.properties").openStream()) {
            Properties properties = new Properties();
            properties.load(props);
            try (KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(properties)) {
                kafkaConsumer.subscribe(Arrays.asList("my-replicated-topic"));
                while (true) {
                    ConsumerRecords<String, String> records = kafkaConsumer.poll(100);
                    for (ConsumerRecord<String, String> record : records) {
                        System.out.printf("offset = %d, value = %s", record.offset(), record.value());
                        System.out.println();
                    }
                }
            }
        }
    }
}
