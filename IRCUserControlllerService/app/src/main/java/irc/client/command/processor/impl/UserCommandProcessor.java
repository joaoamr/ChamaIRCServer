package irc.client.command.processor.impl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import irc.client.command.processor.IRCCommandOrigin;
import irc.client.command.processor.IRCCommandProcessor;
import irc.client.command.processor.IRCCommand;
import irc.client.controller.IRCClientController;

@IRCCommand(token="USER",  origin=IRCCommandOrigin.CLIENT)
public class UserCommandProcessor extends IRCCommandProcessor{
	
	private static Logger logger = LoggerFactory.getLogger(IRCCommandProcessor.class);
	
	public UserCommandProcessor(IRCClientController ircClientController) {
		super(ircClientController);
	}

	@Override
	public void proccess(String command) throws Exception {
		if(ircClientController.isRegisterCompleted())
			return;
		
		try {
			String proc = command.replace("USER", "");
			String[] tokens = proc.split("\\s+");
			String username = tokens[0].trim();
			String name = tokens[3].trim().replace(":","");
			ircClientController.registerUser(username, name);
		} catch (Exception e) {
			logger.warn("Invalid USER command. " + command);
		}
	}

}
