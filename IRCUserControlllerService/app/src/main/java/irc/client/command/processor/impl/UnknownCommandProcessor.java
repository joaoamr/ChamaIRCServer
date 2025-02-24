package irc.client.command.processor.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import irc.client.command.processor.IRCCommandProcessor;
import irc.client.controller.IRCClientController;

public class UnknownCommandProcessor extends IRCCommandProcessor{
	
	private static Logger logger = LoggerFactory.getLogger(UnknownCommandProcessor.class);
	
	public UnknownCommandProcessor(IRCClientController ircClientController) {
		super(ircClientController);
	}

	@Override
	public void proccess(String command) throws Exception {
		logger.debug("Unknown command [" + command + "] from " + ircClientController);
	}

}
