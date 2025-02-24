package irc.client.command.processor.impl;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import irc.client.command.processor.IRCCommandOrigin;
import irc.client.command.processor.IRCCommandProcessor;
import irc.client.command.processor.IRCCommand;
import irc.client.controller.IRCClientController;
import irc.server.messaging.kafka.KafkaProducerFactory;
import irc.server.utils.IRCServerConstants;

@IRCCommand(token = "PRIVMSG", origin = IRCCommandOrigin.CLIENT)
public class PrivateMessageCommandProcessor extends IRCCommandProcessor {

	public PrivateMessageCommandProcessor(IRCClientController ircClientController) {
		super(ircClientController);
	}

	@Override
	public void proccess(String command) throws Exception {
		try (KafkaProducer<String, String> kafkaProducer = KafkaProducerFactory.getProducer()) {
			String[] tokens = command.split("\\s+");
			String target = tokens[1];
			String msg = ":" + ircClientController.getNick() + " " + command;
			String topic = IRCServerConstants.PVT_MESAGE_TOPIC_PREFIX + target;
			ProducerRecord<String, String> record = new ProducerRecord<>(topic, msg);
			kafkaProducer.send(record);
		}
	}

}
