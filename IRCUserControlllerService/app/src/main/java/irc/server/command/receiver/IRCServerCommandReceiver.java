package irc.server.command.receiver;

import java.time.Duration;
import java.util.Arrays;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import irc.client.command.processor.IRCCommandProcessor;
import irc.client.command.processor.IRCCommandProcessorFactory;
import irc.client.controller.IRCClientController;
import irc.server.messaging.kafka.KafkaAdminFactory;
import irc.server.messaging.kafka.KafkaConsumerFactory;
import irc.server.utils.IRCServerConstants;

public class IRCServerCommandReceiver extends Thread {

	private IRCClientController client;
	private static Logger logger = LoggerFactory.getLogger(IRCServerCommandReceiver.class);
	private AdminClient adminClient = KafkaAdminFactory.getAdminClient();
	private boolean isAlive;
	
	public IRCServerCommandReceiver(IRCClientController client) {
		super();
		this.client = client;
		KafkaConsumerFactory.getBroadcastConsumer().register(this);
		clear();
	}

	@Override
	public void run() {
		isAlive = true;
		KafkaConsumer<String, String> kafkaConsumer = KafkaConsumerFactory.getConsumer();
		kafkaConsumer.subscribe(Arrays.asList(topic()));
		
		while (isAlive) {
			try {
				ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofSeconds(5));

				for (ConsumerRecord<String, String> record : records) {
					try {
						String message = record.value();
						process(message);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}

		kafkaConsumer.close();
		clear();
	}
	
	public void process(String message) throws Exception {
		IRCCommandProcessor processor = IRCCommandProcessorFactory.getServerProcessor(message, client);
		processor.proccess(message);
	}
	
	public void stopService() {
		isAlive = false;
	}

	public void clear() {
		if (client.hasNick())
			adminClient.deleteTopics(Arrays.asList(topic()));
	}

	private String topic() {
		return IRCServerConstants.SERVER_COMMAND_RECEIVER_TOPIC_PREFIX + client.getNick();
	}

}
