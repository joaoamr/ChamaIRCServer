package irc.server;

import java.time.Duration;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import irc.server.messaging.kafka.KafkaAdminFactory;
import irc.server.messaging.kafka.KafkaConsumerFactory;
import irc.server.messaging.kafka.KafkaProducerFactory;
import irc.server.messaging.kafka.KafkaPropertiesFactory;
import irc.server.utils.IRCServerConstants;

public class ResourceUnlockService extends Thread {

	private KafkaConsumer<String, String> nicksLockConsumer = null;
	private KafkaConsumer<String, String> semaphoreConsumer = KafkaConsumerFactory.getConsumer();
	private KafkaProducer<String, String> semaphoreProducer = KafkaProducerFactory.getProducer();
	private AdminClient adminClient = KafkaAdminFactory.getAdminClient();
	private static long frequencyTimeInMilis = IRCServerConstants.CONNECTION_CHECK_FREQUENCY;

	private static Pattern locksPattern = Pattern.compile(IRCServerConstants.NICK_LOCK_TOPIC_PREFIX + ".*");
	private static Logger logger = LoggerFactory.getLogger(ResourceUnlockService.class);

	public ResourceUnlockService() {
		Properties properties = KafkaPropertiesFactory.getProperties();
		properties.put(ConsumerConfig.GROUP_ID_CONFIG, "nicks-lock-consumers");
		properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
		properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		nicksLockConsumer = new KafkaConsumer<>(properties);

		semaphoreConsumer.subscribe(Arrays.asList(IRCServerConstants.RESOURCE_UNLOCK_SEMAPHORE_TOPIC));

	}

	@Override
	public void run() {
		Random rnd = new Random();
		Long randomStartWait = rnd.nextLong(frequencyTimeInMilis);

		try {
			Thread.sleep(randomStartWait);
		} catch (InterruptedException e) {
		}

		while (true) {
			try {
				semaphoreProducer.send(new ProducerRecord<String, String>(
						IRCServerConstants.RESOURCE_UNLOCK_SEMAPHORE_TOPIC, "acquire")).get();
				
				ConsumerRecords<String, String> semaphoreRecord = semaphoreConsumer.poll(Duration.ofSeconds(10));

				if (semaphoreRecord.isEmpty()) {
					try {
						Thread.sleep(frequencyTimeInMilis);
					} catch (InterruptedException e) {
					}
					continue;
				}

				nicksLockConsumer.subscribe(locksPattern);
				ConsumerRecords<String, String> records = nicksLockConsumer.poll(Duration.ofSeconds(10));
				Map<String, String> lockedNicks = new HashMap<>();
				
				for(ConsumerRecord<String, String> record : records) {
					String nick = record.topic().split("-")[2];
					if(record.value().startsWith("ping-")) {
						lockedNicks.put(nick, record.value().replace("ping-", ""));
					} else {
						lockedNicks.putIfAbsent(nick, record.value().replace("lock-", ""));
					}
				}

				if (lockedNicks.isEmpty()) {
					try {
						Thread.sleep(frequencyTimeInMilis);
					} catch (InterruptedException e) {
					}
					continue;
				}

				Set<String> topicsToDelete = new HashSet<>();
				
				for(Map.Entry<String, String> record : lockedNicks.entrySet()) {
					try {
						Long time = Long.parseLong(record.getValue());
						String nick = record.getKey();
						if (Calendar.getInstance().getTimeInMillis() - time > frequencyTimeInMilis * 2) {
							topicsToDelete.add(IRCServerConstants.NICK_LOCK_TOPIC_PREFIX + nick);
						}
						
					} catch (Exception e) {
						logger.error("Error on read data " + record.getValue() + " from topic " + record.getKey(), e);
						topicsToDelete.add(record.getKey());
					}
				}

				adminClient.deleteTopics(topicsToDelete);
				adminClient.deleteTopics(Arrays.asList(IRCServerConstants.RESOURCE_UNLOCK_SEMAPHORE_TOPIC));

				try {
					Thread.sleep(frequencyTimeInMilis);
				} catch (InterruptedException e) {
				}

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

}
