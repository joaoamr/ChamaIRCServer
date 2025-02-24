package irc.server.messaging.kafka;

import java.util.Properties;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

public class KafkaPropertiesFactory {
	
	private static Properties properties = new Properties();
	private static String kafkaProducerServer = System.getProperty("kafka.producer.bootstrap.servers", "localhost:9092");
	private static String kafkaConsumerServer = System.getProperty("kafka.consumer.bootstrap.servers", "localhost:9092");
	private static String kafkaAdminServer = System.getProperty("kafka.admin.bootstrap.servers", "localhost:9092");

	
	static {
		properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProducerServer);
		properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

		properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConsumerServer);
		properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		properties.put(ConsumerConfig.GROUP_ID_CONFIG, "consumers");
		
		properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaAdminServer);

	}
	
	public static Properties getProperties() {
		return properties;
	}
	
}
