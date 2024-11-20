package com.github.YoussefHamedd.BIGDATA.Kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class FileToKafkaProducer {

    private static final String TOPIC = "sentences";
    private static final String BOOTSTRAP_SERVERS = "localhost:29092";

    public static void main(String[] args) {
        // Check if file path is provided as an argument
        if (args.length != 1) {
    System.out.println("Usage: ./mvnw compile exec:java -Dexec.mainClass=\"com.github.YoussefHamedd.BIGDATA.Kafka.FileToKafkaProducer\" -Dexec.args=\"/home/ec2-user/environment/Project-BIGDATA/Sample.txt\"");

            return;
        }

        String filePath = args[0];

        // Configure Kafka Producer
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        // Read the file and send each line to Kafka
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Send each line to Kafka topic "sentences"
                ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, line);
                producer.send(record);
                System.out.println("Sent to Kafka: " + line);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } finally {
            // Close the Kafka producer
            producer.close();
        }
    }
}
