package academy.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

import academy.kafka.entities.PaymentRequest;

import java.time.Instant;
import java.time.ZoneId;

public class PaymentRequestTimeExtractor implements TimestampExtractor {
    @Override
    public long extract(ConsumerRecord<Object, Object> consumerRecord, long prevTime) {
        PaymentRequest pr = (PaymentRequest) consumerRecord.value();
        Instant instant = pr.getPeriodStart().atStartOfDay(ZoneId.systemDefault()).toInstant();
        long eventTime = instant.toEpochMilli();
        return ((eventTime > 0) ? eventTime : prevTime);
    }
}
