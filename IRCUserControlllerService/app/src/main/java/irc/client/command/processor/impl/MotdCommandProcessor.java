package irc.client.command.processor.impl;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import irc.client.command.processor.IRCCommandOrigin;
import irc.client.command.processor.IRCCommandProcessor;
import irc.client.command.processor.IRCCommand;
import irc.client.controller.IRCClientController;
import irc.server.messaging.kafka.KafkaPropertiesFactory;
import irc.server.utils.IRCServerConstants;

@IRCCommand(token="MOTD", origin=IRCCommandOrigin.CLIENT)
public class MotdCommandProcessor extends IRCCommandProcessor{
	
	private static Logger logger = LoggerFactory.getLogger(MotdCommandProcessor.class);
	
	private static final String START_NB = "375";
	
	private static final String MID_NB = "372";
	
	private static final String END_NB = "376";
	
	public MotdCommandProcessor(IRCClientController ircClientController) {
		super(ircClientController);
	}

	@Override
	public void proccess(String command) throws Exception {
		sendMotd();
	}
	
	public void sendMotd() {
		if(!ircClientController.hasNick()) //Ignore if user is not registered
			return;
		
		String nick = ircClientController.getNick();
		
		Properties props = new Properties();
		props.putAll(KafkaPropertiesFactory.getProperties());
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "motd-consumers");
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
			StringBuilder motdMsg = new StringBuilder();
			consumer.subscribe(Arrays.asList(new String[] { IRCServerConstants.SERVER_MOTD_TOPIC }));
			consumer.poll(Duration.ofMillis(250)).forEach(entry -> {
				String number = START_NB;
				if(!motdMsg.isEmpty()) {
					motdMsg.append("\n");
					number = MID_NB;
				}
				String message = format(entry.value(), number);
				motdMsg.append(message);
			});
			if (!motdMsg.isEmpty()) {
				ircClientController.send(motdMsg.toString());
				String endMessage = format("End of /MOTD command.", END_NB);
				ircClientController.send(endMessage);
			}
		} catch (Exception e) {
			logger.error("Error on send MOTD to user " + nick, e);
		}
	}
	
	private String format(String message, String number) {
		String pre = ":" + IRCServerConstants.HOST_NAME + " " + number + " " + ircClientController.getNick() + " :";
		return pre + message;
	}
}
