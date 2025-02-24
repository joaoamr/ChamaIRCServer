package irc.server.messaging.kafka;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import irc.server.messaging.kafka.consumer.IRCBroadcastConsumer;

public class KafkaConsumerFactory {
		
	private static IRCBroadcastConsumer broadcastConsumer = null;
	
	public static KafkaConsumer<String, String> getConsumer() {
		return new KafkaConsumer<>(KafkaPropertiesFactory.getProperties());
	}
	
	public static IRCBroadcastConsumer getBroadcastConsumer() {
		if(broadcastConsumer == null) {
			broadcastConsumer = new IRCBroadcastConsumer();
			broadcastConsumer.start();
		}
		
		return broadcastConsumer;
	}
	
}
