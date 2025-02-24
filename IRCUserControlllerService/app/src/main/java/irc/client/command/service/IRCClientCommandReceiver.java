package irc.client.command.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import irc.client.command.processor.IRCCommandProcessor;
import irc.client.command.processor.IRCCommandProcessorFactory;
import irc.client.controller.IRCClientController;

public class IRCClientCommandReceiver extends Thread {
	
	private IRCClientController client;
	private static Logger logger = LoggerFactory.getLogger(IRCClientCommandReceiver.class);

	public IRCClientCommandReceiver(IRCClientController client) {
		super();
		this.client = client;
	}

	@Override
	public void run() {
		Socket clientSocket = client.getClientSocket();
		
		try (InputStream input = clientSocket.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(input));
				OutputStream output = clientSocket.getOutputStream();
				PrintWriter writer = new PrintWriter(output, true)) {
			String message;
			while ((message = reader.readLine()) != null) {
				IRCCommandProcessor processor = IRCCommandProcessorFactory.getClientProcessor(message, client);
				client.setLastMessageTime(Calendar.getInstance().getTimeInMillis());
				processor.proccess(message);
			}
		} catch (IOException e) {
			logger.debug("Error while receive data from client [" + clientSocket.getInetAddress() + "]");
		} catch (Exception e) {
			logger.debug("Error while processing data from client [" + clientSocket.getInetAddress() + "]", e);
		} finally {
			client.kill();
		}
	}

}
