package irc.server.messaging.kafka.consumer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import irc.server.command.receiver.IRCServerCommandReceiver;
import irc.server.messaging.kafka.KafkaPropertiesFactory;
import irc.server.utils.IRCServerConstants;

public class IRCBroadcastConsumer extends Thread {

	private Set<IRCServerCommandReceiver> listeners = new HashSet<>();
	private Object guard = new Object();
	private KafkaConsumer<String, String> consumer;
	private Logger logger = LoggerFactory.getLogger(IRCBroadcastConsumer.class);
	
	public IRCBroadcastConsumer() {
		Random rnd = new Random();
		String groupId = "server-broadcast-consumer-" + Calendar.getInstance().getTimeInMillis() + "-"
				+ rnd.nextInt(1000);

		Properties props = new Properties();
		props.putAll(KafkaPropertiesFactory.getProperties());
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

		consumer = new KafkaConsumer<>(props);
		consumer.subscribe(Arrays.asList(IRCServerConstants.SERVER_COMMAND_BROADCAST_TOPIC));
	}

	@Override
	public void run() {
		while (true) {
			try {
				ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
				for (ConsumerRecord<String, String> record : records) {
					Set<IRCServerCommandReceiver> listeners = new HashSet<>(this.listeners);
					for (IRCServerCommandReceiver receiver : listeners) {
						if (receiver.isAlive()) {
							try {
								receiver.process(record.value());
							} catch (Exception e) {
								logger.error(e.getMessage(), e);
							}
						} else {
							synchronized (guard) {
								this.listeners.remove(receiver);
							}
						}
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public void register(IRCServerCommandReceiver receiver) {
		synchronized (guard) {
			listeners.add(receiver);
		}
	}

}
