package irc.server.messaging.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;

public class KafkaProducerFactory {
		
	public static KafkaProducer<String, String> getProducer() {
		return new KafkaProducer<>(KafkaPropertiesFactory.getProperties());
	}
	
}
