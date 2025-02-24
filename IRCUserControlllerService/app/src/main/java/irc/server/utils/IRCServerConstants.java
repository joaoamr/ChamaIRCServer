package irc.server.utils;

public class IRCServerConstants {
	public static final String SERVER_COMMAND_BROADCAST_TOPIC = "server-command-broadcast";
	public static final String SERVER_COMMAND_RECEIVER_TOPIC_PREFIX = "server-command-unicast-";
	public static final String NICK_LOCK_TOPIC_PREFIX = "locks-nick-";
	public static final String USERNAME_REGISTER = "username-register-";
	public static final String PVT_MESAGE_TOPIC_PREFIX = "messages-private-";
	public static final String SERVER_MOTD_TOPIC = "server-motd";
	public static final String RESOURCE_UNLOCK_SEMAPHORE_TOPIC = "resource-unlock-semaphore";
	public static final String HOST_NAME = System.getProperty("irc.server.hostname", "chama.irc.server");
	
	public static final Long CONNECTION_CHECK_FREQUENCY = Long.parseLong(System.getProperty("connection.check.frequency", "150000"));
}
