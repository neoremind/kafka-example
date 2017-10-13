# Kafka-example

Based on [Kafka 0.9.0](https://kafka.apache.org/090/documentation.html#introduction)

Set up zookeeper at localhost with port = 2182

Set up 4 brokers
```
server-1, port=9093
server-2, port=9094
server-3, port=9095
server-4, port=9096
```

Start 4 brokers
```
bin/kafka-server-start.sh config/server-1.properties &
bin/kafka-server-start.sh config/server-2.properties &
bin/kafka-server-start.sh config/server-3.properties &
bin/kafka-server-start.sh config/server-4.properties &
```

Create a topic
```
bin/kafka-topics.sh --create --zookeeper localhost:2182 --replication-factor 3 --partitions 3 --topic my-replicated-topic
```

Show topic
```
bin/kafka-topics.sh --describe --zookeeper localhost:2182 --topic my-replicated-topic
Topic:my-replicated-topic	PartitionCount:3	ReplicationFactor:3	Configs:
	Topic: my-replicated-topic	Partition: 0	Leader: 4	Replicas: 4,1,2	Isr: 4,1,2
	Topic: my-replicated-topic	Partition: 1	Leader: 1	Replicas: 1,2,3	Isr: 1,2,3
	Topic: my-replicated-topic	Partition: 2	Leader: 2	Replicas: 2,3,4	Isr: 2,3,4
```

Publish some messages
```
bin/kafka-console-producer.sh --broker-list localhost:9093 --topic my-replicated-topic
```

Consume these messages
```
bin/kafka-console-consumer.sh --zookeeper localhost:2182 --from-beginning --topic my-replicated-topic
```

Another way to consume messages
```
bin/kafka-run-class.sh kafka.tools.SimpleConsumerShell --broker-list localhost:9093,localhost:9094,localhost:9095,localhost:9096 --offset 0 --partition 0 --topic my-replicated-topic
```

You can get offset by executing the following command
```
bin/kafka-run-class.sh kafka.tools.GetOffsetShell  --broker-list localhost:9093,localhost:9094,localhost:9095,localhost:9096 --topic my-replicated-topic --time -1 --offsets 1
```

Run consumer example code in this project.

See how messages are consumed by consumer group.
```
bin/kafka-run-class.sh kafka.tools.ConsumerOffsetChecker --group my-replicated-test --zookeeper localhost:2182 --topic my-replicated-topic
or
bin/kafka-consumer-offset-checker.sh --group my-replicated-test --topic my-replicated-topic --zookeeper localhost:2182

Group           Topic                          Pid Offset          logSize         Lag             Owner
my-replicated-test my-replicated-topic            0   7               7               0               none
my-replicated-test my-replicated-topic            1   9               9               0               none
my-replicated-test my-replicated-topic            2   17              17              0               none
```

Which means Does this mean that there are a total of 17 messages out of which 17 have already been read and 0 are pending to be read. But up that is almost right. Kafka saves the offset from time to time (it depends on settings). It doesn't save the offset after every message has been read or processed. Thus it may means that the Consumer haven't read the last three messages yet but it may also means that it has read all pending messages but the offset in ZK hasn't been updated yet.

```
bin/kafka-run-class.sh kafka.tools.UpdateOffsetsInZK earliest config/consumer.properties my-replicated-topic
```

