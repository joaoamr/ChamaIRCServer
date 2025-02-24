package irc.server.messaging.kafka.consumer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Properties;
import java.util.Random;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import irc.server.messaging.IRCServerMessageReceiver;
import irc.server.messaging.kafka.KafkaPropertiesFactory;
import irc.server.utils.IRCServerConstants;

public class IRCPrivateMessageKafkaConsumer extends Thread {

	private IRCServerMessageReceiver receiver;
	
	public IRCPrivateMessageKafkaConsumer(IRCServerMessageReceiver receiver) {
		super();
		this.receiver = receiver;
	}

	@Override
	public void run() {
		String oldNick = null;
		Random rnd = new Random();
		String groupId = "private-message-consumer-" + Calendar.getInstance().getTimeInMillis() + "-"
				+ rnd.nextInt(1000);

		Properties props = new Properties();
		props.putAll(KafkaPropertiesFactory.getProperties());
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		
		try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
			while (receiver.isAlive()) {
				if(receiver.getNick() == null) {
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
					}
					continue;
				}
				
				if(oldNick == null) {
					oldNick = receiver.getNick();
					consumer.subscribe(Arrays.asList(IRCServerConstants.PVT_MESAGE_TOPIC_PREFIX + receiver.getNick()));
				} else {
					if(!oldNick.equals(receiver.getNick())) {
						consumer.subscribe(Arrays.asList(IRCServerConstants.PVT_MESAGE_TOPIC_PREFIX + receiver.getNick()));
					}
				}
				
				ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
				for (ConsumerRecord<String, String> record : records) {
					receiver.processUserMessage(record.value());
				}
			}
		}
	}
}
