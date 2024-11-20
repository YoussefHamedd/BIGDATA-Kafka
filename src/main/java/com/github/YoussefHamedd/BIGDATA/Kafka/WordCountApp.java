package com.github.YoussefHamedd.BIGDATA.Kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import java.util.Arrays;
import java.util.Properties;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.LongSerializer;

public class WordCountApp {
    
    
    
        // Global counters and maps
    private static final AtomicLong totalLines = new AtomicLong(0);
    private static final AtomicLong totalCharactersProcessed = new AtomicLong(0);
    private static final AtomicLong cursorPosition = new AtomicLong(0);
    private static final Map<String, Long> wordCountMap = new HashMap<>();
    private static final String CSV_FILE_PATH = "StreamingResults.csv";
    private static KafkaProducer<String, Long> producer;
    


    public static void main(String[] args) {
        Properties props = getConfig();
        Properties producerProps = getProducerConfig();
        producer = new KafkaProducer<>(producerProps);

        StreamsBuilder streamsBuilder = new StreamsBuilder();
                
        // Step 1: Consume from 'sentences' topic
        KStream<String, String> sentencesStream = streamsBuilder.stream("sentences");
        
        // Step 2: Process each line
        sentencesStream.foreach((key, sentence) -> {
            long lineNumber = totalLines.incrementAndGet(); // Increment line number
            long charCountInLine = sentence.length();
            long cumulativeCharCount = totalCharactersProcessed.addAndGet(charCountInLine); // Total characters processed

        // Track cursor position (cumulative across lines)
            long startingCursorPos = cursorPosition.get();
            
        
        
        
        // Step 3: Count word occurrences and track their positions
        String[] words = sentence.toLowerCase(Locale.ROOT).split("\\W+");
        for (String word : words) {
            wordCountMap.put(word, wordCountMap.getOrDefault(word, 0L) + 1);

            // Export each word occurrence along with its cursor position to CSV
                exportToCSV(lineNumber, cumulativeCharCount, word, wordCountMap.get(word), startingCursorPos);
                
            // Produce the result to the 'words' topic
                producer.send(new ProducerRecord<>("word-count", word, wordCountMap.get(word)));


            // Update cursor position for the next word
            startingCursorPos += word.length() + 1; // +1 for the space after each word
            cursorPosition.addAndGet(word.length() + 1); // Update global cursor position
            }
        });

 
    

 
                
        // Step 6: Start the Kafka Streams application        
        // Create the Kafka Streams Application
        KafkaStreams kafkaStreams = new KafkaStreams(streamsBuilder.build(), props);
        // Start the application
        kafkaStreams.start();

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            kafkaStreams.close();
            producer.close();
        }));
    }

    private static Properties getConfig() {
        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "word-count-app");
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0");
        return properties;
    }
    
    
        private static Properties getProducerConfig() {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        return properties;
    }
    

    // Method to export results to a CSV file
    private static void exportToCSV(long lineNumber, long totalCharsProcessed, String word, long wordCount, long cursorCharacterPos) {
        File csvFile = new File(CSV_FILE_PATH);
        boolean fileExists = csvFile.exists();

        try (FileWriter csvWriter = new FileWriter(csvFile, true)) {
            // Write header only if the file is being created for the first time
            if (!fileExists) {
                csvWriter.append("Line Number,Total Characters Processed,Word,Word Count,Cursor Character Position\n");
            }

            // Write the data
            csvWriter.append(String.valueOf(lineNumber))
                     .append(",")
                     .append(String.valueOf(totalCharsProcessed))
                     .append(",")
                     .append(word)
                     .append(",")
                     .append(String.valueOf(wordCount))
                     .append(",")
                     .append(String.valueOf(cursorCharacterPos))
                     .append("\n");
            csvWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}