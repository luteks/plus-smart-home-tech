package ru.yandex.practicum.kafka;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Properties;

@Configuration
@PropertySource("classpath:application.yaml")
public class CollectorProducerConfig {

    @Value("${collector.kafka.producer.properties.bootstrap.servers}")
    private String bootstrapServers;

    @Value("${collector.kafka.producer.properties.key.serializer}")
    private String keySerializerClass;

    @Value("${collector.kafka.producer.properties.value.serializer}")
    private String valueSerializerClass;

    @Bean
    public CollectorClientProducer getProducer() {
        return new CollectorClientProducer() {
            private Producer<String, SpecificRecordBase> producer;

            @Override
            public Producer<String, SpecificRecordBase> getProducer() {
                if (producer == null) {
                    initProducer();
                }
                return producer;
            }

            private void initProducer() {
                Properties config = new Properties();
                config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
                config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializerClass);
                config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializerClass);

                producer = new KafkaProducer<>(config);
            }

            @Override
            public void stop() {
                if (producer != null) {
                    producer.flush();
                    producer.close();
                }
            }
        };
    }
}