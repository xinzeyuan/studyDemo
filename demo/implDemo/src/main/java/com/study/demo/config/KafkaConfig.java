package com.study.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.util.HashMap;
import java.util.Map;

/**
 * @program: direct-business
 * @description
 * @author:
 * @create: 2019-08-08 19:34
 **/
@Slf4j
@Configuration
public class KafkaConfig {

    //@Value("${kafka.consumer.bootstrap.servers}")
    private String servers="localhost:9092";

    //@Value("${kafka.consumer.group.id}")
    private String groupId="testGroup";

    //@Value("${kafka.consumer.session.timeout.ms}")
    private String sessionTimeout="12000";

    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeout);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        return props;
    }

    /** 获取工厂 */
    public ConsumerFactory<Long, byte[]> consumerFactory() {
        return new DefaultKafkaConsumerFactory<Long, byte[]>(consumerConfigs());
    }

    /** 获取实例 */
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<Long, byte[]>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<Long, byte[]> factory = new ConcurrentKafkaListenerContainerFactory<Long, byte[]>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);
        factory.getContainerProperties().setPollTimeout(3000);
        return factory;
    }


    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<String, Object>(16);
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.RETRIES_CONFIG, "1");
        return props;
    }


    public ProducerFactory<Long, byte[]> kafkaProducerFactory() {
        return new DefaultKafkaProducerFactory<Long, byte[]>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<Long, byte[]> kafkaTemplate() {
        return new KafkaTemplate<Long, byte[]>(kafkaProducerFactory());
    }

}
