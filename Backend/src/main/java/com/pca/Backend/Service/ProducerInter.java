package com.pca.Backend.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;

import com.pca.Backend.Utils.Utils;

@Service
public class ProducerInter implements ProducerInterceptor<String, String>{

    @Override
    public void configure(Map<String, ?> configs) {
       
    }
    @Override
    public ProducerRecord<String, String> onSend(ProducerRecord<String, String> record) {
         record.headers().add("correlationId", Utils.generateCorrelationId().getBytes(StandardCharsets.UTF_8));
         return record;
    }

    @Override
    public void close() {
        
    }

}
