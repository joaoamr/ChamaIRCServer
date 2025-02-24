package irc.client.command.processor.impl;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import irc.client.command.processor.IRCCommandOrigin;
import irc.client.command.processor.IRCCommandProcessor;
import irc.client.command.processor.IRCCommand;
import irc.client.controller.IRCClientController;
import irc.server.messaging.kafka.KafkaAdminFactory;
import irc.server.messaging.kafka.KafkaProducerFactory;
import irc.server.utils.IRCServerConstants;

@IRCCommand(token = "NICK", origin = IRCCommandOrigin.CLIENT)
public class NickCommandProcessor extends IRCCommandProcessor {

	private static Logger logger = LoggerFactory.getLogger(NickCommandProcessor.class);

	private static Set<Character> fobiddenCharacters = null;

	static {
		if (fobiddenCharacters == null) {
			fobiddenCharacters = new HashSet<>();
			String prop = System.getProperty("irc.server.nick.fobidden.characters", "@;,:!+#");
			for (int i = 0; i < prop.length(); i++)
				fobiddenCharacters.add(prop.charAt(i));
		}
	}

	private static short nickMaxLen = Short.parseShort(System.getProperty("irc.server.nick.max.len", "20"));

	public NickCommandProcessor(IRCClientController ircClientController) {
		super(ircClientController);
	}

	@Override
	public void proccess(String command) throws Exception {
		try (KafkaProducer<String, String> kafkaProducer = KafkaProducerFactory.getProducer()) {
			String[] tokens = command.split("\\s+");
			String newNick = tokens[1].replace(":", "");

			if (newNick.length() > nickMaxLen) { // Truncate if it exceeds the maximum size
				newNick = newNick.substring(0, nickMaxLen);
			}

			if (this.ircClientController.hasNick() && newNick.equals(this.ircClientController.getNick())) {
				return;
			}

			if (!isValid(newNick)) {
				String errorMessage = "432 * " + newNick + " :Nickname contains invalid characters.";
				ircClientController.send(errorMessage);
				return;
			}

			boolean avail = acquire(newNick);

			if (avail) {
				String topic = IRCServerConstants.NICK_LOCK_TOPIC_PREFIX + newNick;
				String ts = "ping-" + String.valueOf(Calendar.getInstance().getTimeInMillis());
				ProducerRecord<String, String> record = new ProducerRecord<>(topic, ts);
				kafkaProducer.send(record);

				release(); // release old Nick
				ircClientController.registerNick(newNick);
				logger.info("User nickname [" + newNick + "]");
			} else {
				String errorMessage = "433 * " + newNick + " :Nickname is already in use.";
				ircClientController.send(errorMessage);
			}
		}
	}

	private boolean acquire(String nick) throws InterruptedException, ExecutionException {
		try (KafkaProducer<String, String> kafkaProducer = KafkaProducerFactory.getProducer()) {
			String ts = "lock-" + String.valueOf(Calendar.getInstance().getTimeInMillis());
			ProducerRecord<String, String> nickLock = new ProducerRecord<>(
					IRCServerConstants.NICK_LOCK_TOPIC_PREFIX + nick, ts);
			RecordMetadata metadata = kafkaProducer.send(nickLock).get();
			if (metadata.offset() != 0) {
				// Wait a bit and try again
				Thread.sleep(5000);
				metadata = kafkaProducer.send(nickLock).get();
				return metadata.offset() == 0;
			} else {
				return true;
			}
		}
	}

	public void release() {
		AdminClient adminClient = KafkaAdminFactory.getAdminClient();
		String nick = ircClientController.getNick();
		if (nick != null)
			adminClient.deleteTopics(Arrays.asList(new String[] { IRCServerConstants.NICK_LOCK_TOPIC_PREFIX + nick }));

	}

	private boolean isValid(String nick) {
		for (int i = 0; i < nick.length(); i++)
			if (fobiddenCharacters.contains(nick.charAt(i)))
				return false;

		return true;
	}

}
