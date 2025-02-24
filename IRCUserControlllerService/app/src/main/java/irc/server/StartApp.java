package irc.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import irc.client.command.processor.IRCCommandProcessorFactory;
import irc.client.controller.IRCClientController;

public class StartApp {
	
    private static int ircServerPort = Integer.parseInt(System.getProperty("irc.server.port", "6667"));
    
    private static Logger logger = LoggerFactory.getLogger(StartApp.class);
	
	public static void main(String[] args) {
		logger.info("System Properties:");
		System.getProperties().forEach((key, value) -> {
			logger.info(key + "=" + value);
		});
		logger.info("Starting IRC Server user controller app at port: " + ircServerPort);
		
		IRCCommandProcessorFactory.start();
		ResourceUnlockService resourceUnlockService = new ResourceUnlockService();
		resourceUnlockService.start();
		
		try (ServerSocket serverSocket = new ServerSocket(ircServerPort)) {
			while (true) {
                Socket clientSocket = serverSocket.accept();
                IRCClientController controller = new IRCClientController(clientSocket);
                controller.start();
			}
		} catch (IOException e) {
			logger.error("Error while try to open IRC connection Socket.", e);
		}
	}
}
