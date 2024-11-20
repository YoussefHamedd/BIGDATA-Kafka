# Kafka Streams Word Application
This project demonstrates a Kafka Streams application that processes sentences and counts the occurrences of each word. It includes a file-to-Kafka producer, a word count processor, and a consumer to display the results in real-time.

## Requirements
Before starting, ensure the following tools and environments are ready:
- **Docker**: For running Kafka and Zookeeper containers.
- **Java 11**: Required to build and run the application.
- **Cloud9 IDE**: Recommended environment, though any IDE with Java support will work.

## Instructions Of the project

### Starting Kafka
*we need to start Kafka. For that we have a [docker-compose.yml file](docker-compose.yml) To start a Zookeeper instance and a Kafka broker. 
*create topics using the script found in the [create-topics.sh](./scripts/create-topics.sh) file.
```shell
docker compose -f ./docker-compose.yml up
```
### starting the application
```shell
 ./mvnw compile exec:java -Dexec.mainClass="com.github.YoussefHamedd.BIGDATA.Kafka.WordCountApp"
```
### Publishing and consuming the results
1.  run commands on a CLI:
```shell
docker exec -it kafka /bin/bash
```
2. we create a console consumer to consume the `word-count` topic:
```shell
kafka-console-consumer --topic word-count --bootstrap-server localhost:9092 \
 --from-beginning \
 --property print.key=true \
 --property key.separator=" : " \
 --key-deserializer "org.apache.kafka.common.serialization.StringDeserializer" \
 --value-deserializer "org.apache.kafka.common.serialization.LongDeserializer"
```
3. Open a new terminal and connect again to the kafka docker container:
```shell
docker exec -it kafka /bin/bash
```
4. Create a console producer to insert sentences in the `sentences` topic:
```shell
kafka-console-producer --topic sentences --bootstrap-server localhost:9092
```
5. In your console producer, insert the following messages:
```
>Hello kafka streams
>Hello world
```
6. In your console consumer terminal, you should see the following result:
```
hello : 1
kafka : 1
streams : 1
hello : 2
world : 1
```
6.Sending file to the Topic
```shell
./mvnw compile exec:java -Dexec.mainClass="com.github.YoussefHamedd.BIGDATA.Kafka.FileToKafkaProducer"
```
8.observe the result on [StreamingResults File](StreamingResults.csv)

