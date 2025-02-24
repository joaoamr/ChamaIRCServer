package irc.server.messaging.kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;

public class KafkaAdminFactory {
	
	private static AdminClient adminClient = KafkaAdminClient.create(KafkaPropertiesFactory.getProperties());

	public static AdminClient getAdminClient() {
		return adminClient;
	}
	
}
