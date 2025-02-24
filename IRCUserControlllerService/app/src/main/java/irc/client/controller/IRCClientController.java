package irc.client.controller;

import java.io.IOException;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Calendar;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import irc.client.command.processor.impl.MotdCommandProcessor;
import irc.client.command.processor.impl.NickCommandProcessor;
import irc.client.command.service.IRCClientCommandReceiver;
import irc.client.command.service.IRCClientConnectionCheckService;
import irc.server.command.receiver.IRCServerCommandReceiver;
import irc.server.messaging.IRCServerMessageReceiver;
import irc.server.messaging.kafka.KafkaAdminFactory;
import irc.server.messaging.kafka.KafkaProducerFactory;
import irc.server.utils.IRCServerConstants;

public class IRCClientController {

	private static Logger logger = LoggerFactory.getLogger(IRCClientController.class);
	private Socket clientSocket;
	private String nick;
	private boolean alive = false;
	private String name;
	private String username;
	private String oldNick;
	private boolean registerCompleted;
	private long lastMessageTime = Calendar.getInstance().getTimeInMillis();

	private IRCServerMessageReceiver messageReceiver = new IRCServerMessageReceiver(this);
	private IRCClientCommandReceiver clientCommandReceiver;
	private IRCClientConnectionCheckService connectionCheck;
	private IRCServerCommandReceiver serverCommandReceiver;
	
	
	public IRCClientController(Socket clientSocket) {
		super();
		this.clientSocket = clientSocket;
	}

	public void start() {
		alive = true;
		this.clientCommandReceiver = new IRCClientCommandReceiver(this);
		this.clientCommandReceiver.start();

		this.connectionCheck = new IRCClientConnectionCheckService(this);
		this.connectionCheck.start();

		this.serverCommandReceiver = new IRCServerCommandReceiver(this);
	}

	public String getNick() {
		return nick;
	}

	public void registerNick(String nick) {
		try {
			this.serverCommandReceiver.clear(); // Clear old nick data
			messageReceiver.clear();
			this.oldNick = this.nick;
			this.nick = nick;
			completeRegister();
		} catch (Exception e) {
			logger.error("Error while try to set nick " + nick, e);
			kill();
		}
	}

	public void registerUser(String user, String name) {
		this.username = user;
		this.name = name;
		completeRegister();
	}

	private void completeRegister() {
		if (!registerCompleted && nick != null && username != null) {
			AdminClient adminClient = KafkaAdminFactory.getAdminClient();
			adminClient.deleteTopics(Arrays.asList(new String[] { IRCServerConstants.USERNAME_REGISTER + nick }));

			KafkaProducer<String, String> producer = KafkaProducerFactory.getProducer();
			ProducerRecord<String, String> userStatement = new ProducerRecord<>(
					IRCServerConstants.USERNAME_REGISTER + nick, username + " " + name);
			producer.send(userStatement);

			send(":" + IRCServerConstants.HOST_NAME + " 001 " + nick + " :");

			MotdCommandProcessor motdProcessor = new MotdCommandProcessor(this);
			motdProcessor.sendMotd();
			serverCommandReceiver.start();
			registerCompleted = true;

		} else if(registerCompleted) {
			String notification = ":" + this.oldNick + " NICK " + nick;
			send(notification);
			try (KafkaProducer<String, String> producer = KafkaProducerFactory.getProducer()) {
				ProducerRecord<String, String> record = new ProducerRecord<>(IRCServerConstants.SERVER_COMMAND_BROADCAST_TOPIC
						, "ECHO " + notification);
				producer.send(record);
			} catch (Exception e) {
				logger.error("Error on notify server nick exchange from " + oldNick + " to " + nick, e);
				kill();
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder attributes = new StringBuilder();
		attributes.append(nick);
		attributes.append(" [" + clientSocket.getInetAddress() + "]");
		return attributes.toString();
	}

	public void kill() {
		logger.info("Client socket will be closed. [" + clientSocket.getInetAddress() + "]");
		try {
			clientSocket.close();
		} catch (IOException e) {
		}

		NickCommandProcessor nickProcessor = new NickCommandProcessor(this);
		nickProcessor.release();
		messageReceiver.clear();

		AdminClient adminClient = KafkaAdminFactory.getAdminClient();
		adminClient.deleteTopics(Arrays.asList(new String[] { IRCServerConstants.USERNAME_REGISTER + nick }));

		this.serverCommandReceiver.stopService();

		alive = false;
	}

	public void send(String message) {
		try {
			OutputStream output = clientSocket.getOutputStream();
			PrintWriter writer = new PrintWriter(output, true);
			writer.println(message);
		} catch (IOException ioe) {
			logger.warn("IOException occurred while trying to send message. The socket will be killed.");
			kill();
		}
	}

	public Socket getClientSocket() {
		return clientSocket;
	}

	public boolean isAlive() {
		return alive;
	}

	public boolean hasNick() {
		return nick != null;
	}

	public boolean isRegisterCompleted() {
		return registerCompleted;
	}

	public long getLastMessageTime() {
		return lastMessageTime;
	}

	public void setLastMessageTime(long lastMessageTime) {
		this.lastMessageTime = lastMessageTime;
	}

}
