package com.neoremind.kafka.example;

import com.google.common.io.Resources;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * https://kafka.apache.org/090/javadoc/index.html?org/apache/kafka/clients/producer/KafkaProducer.html
 * <p>
 * Why configure broker addresses instead of zookeeper?
 * https://stackoverflow.com/questions/22444351/why-does-kafka-producer-take-a-broker-endpoint-when-being-initialized
 * -instead-of
 * <p>
 * “metadata.broker.list” defines where the Producer can find a one or more Brokers to determine the Leader for each
 * topic. This does not need to be the full set of Brokers in your cluster but should include at least two in case
 * the first Broker is not available. No need to worry about figuring out which Broker is the leader for the topic
 * (and partition), the Producer knows how to connect to the Broker and ask for the meta data then connect to the
 * correct Broker.
 * <p>
 * First of all, zookeeper is needed only for high level consumer. SimpleConsumer does not require zookeeper to work.
 * <p>
 * The main reason zookeeper is needed for a high level consumer is to track consumed offsets and handle load balancing.
 * <p>
 * https://stackoverflow.com/questions/29511521/whether-key-is-required-as-part-of-sending-message-in-kafka
 * <p>
 * If no key and partition specified, RR will be used to balance between partitions.
 * <p>
 * If no partition, attaching a key to messages will ensure messages with the same key always go to the same
 * partition in a topic. Kafka guarantees order within a partition, but not across partitions in a topic, so
 * alternatively not providing a key - which will result in round-robin distribution across partitions - will not
 * maintain such order.
 * <p>
 * Otherwise, null keys may provide better distribution and prevent potential hot spotting issues in cases where some
 * keys may appear more than others.
 * <p>
 * From https://cwiki.apache.org/confluence/display/KAFKA/A+Guide+To+The+Kafka+Protocol
 * Semantic partitioning means using some key in the message to assign messages to partitions. For example if you
 * were processing a click message stream you might want to partition the stream by the user id so that all data for
 * a particular user would go to a single consumer. To accomplish this the client can take a key associated with the
 * message and use some hash of this key to choose the partition to which to deliver the message.
 * <p>
 * 如果想保证userid level的有序，那么必须指定key，这样就可以分配到同一个partition。
 *
 * @author xu.zhang
 */
public class SimpleProducer {

    public static final int MSG_COUNT = 10;

    public static void main(String[] args) throws IOException, InterruptedException {
        // set up the producer
        try (InputStream props = Resources.getResource("producer.properties").openStream()) {
            Properties properties = new Properties();
            properties.load(props);
            try (KafkaProducer<String, String> producer = new KafkaProducer<>(properties)) {
                for (int i = 0; i < MSG_COUNT; i++) {
                    producer.send(new ProducerRecord<String, String>(
                            "my-replicated-topic",
                            String.format("{\"type\":\"test\", \"t\":%.3f, \"k\":%d}", System.nanoTime() * 1e-9, i)));

                    // every so often send to a different topic
                    if (i % 1000 == 0) {
                        producer.send(new ProducerRecord<String, String>(
                                "my-replicated-topic",
                                String.format("{\"type\":\"marker\", \"t\":%.3f, \"k\":%d}", System.nanoTime() *
                                        1e-9, i)));
                        producer.send(new ProducerRecord<String, String>(
                                "summary-markers",
                                String.format("{\"type\":\"other\", \"t\":%.3f, \"k\":%d}", System.nanoTime() * 1e-9,
                                        i)));
                        producer.flush();
                        System.out.println("Sent msg number " + i);
                    }
                }
            }
        }
    }

}
