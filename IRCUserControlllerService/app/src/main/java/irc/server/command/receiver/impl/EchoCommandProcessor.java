package irc.server.command.receiver.impl;

import irc.client.command.processor.IRCCommandOrigin;
import irc.client.command.processor.IRCCommandProcessor;
import irc.client.command.processor.IRCCommand;
import irc.client.controller.IRCClientController;

@IRCCommand(token="ECHO",  origin=IRCCommandOrigin.SERVER)
public class EchoCommandProcessor extends IRCCommandProcessor{
		
	public EchoCommandProcessor(IRCClientController ircClientController) {
		super(ircClientController);
	}

	@Override
	public void proccess(String command) throws Exception {
		String message = command.replace("ECHO", "").trim();
		if(!message.isBlank()) {
			ircClientController.send(message);
		}
	}

}
