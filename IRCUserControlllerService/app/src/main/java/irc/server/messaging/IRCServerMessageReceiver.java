package irc.server.messaging;

import java.util.Arrays;

import org.apache.kafka.clients.admin.AdminClient;
import irc.client.controller.IRCClientController;
import irc.server.messaging.kafka.KafkaAdminFactory;
import irc.server.messaging.kafka.consumer.IRCPrivateMessageKafkaConsumer;
import irc.server.utils.IRCServerConstants;

public class IRCServerMessageReceiver {
	
	private IRCClientController client;
	private IRCPrivateMessageKafkaConsumer pvtMessageConsumer;

	public IRCServerMessageReceiver(IRCClientController client) {
		super();
		this.client = client;
		pvtMessageConsumer = new IRCPrivateMessageKafkaConsumer(this);
		pvtMessageConsumer.start();
	}
	
	public void clear() {
		AdminClient adminClient = KafkaAdminFactory.getAdminClient();
		String nick = client.getNick();
		if(nick == null)
			return;
		adminClient.deleteTopics(Arrays.asList(new String[] {IRCServerConstants.PVT_MESAGE_TOPIC_PREFIX + nick}));

	}
	
	public void processUserMessage(String messageLine) {
		client.send(messageLine);
	}
	
	public boolean isAlive() {
		return client.isAlive();
	}
	
	public String getNick() {
		return client.getNick();
	}
}
