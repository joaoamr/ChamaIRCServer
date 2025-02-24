package irc.server.command.receiver.impl;

import irc.client.command.processor.IRCCommandOrigin;
import irc.client.command.processor.IRCCommandProcessor;
import irc.client.command.processor.IRCCommand;
import irc.client.controller.IRCClientController;

@IRCCommand(token="KILL",  origin=IRCCommandOrigin.SERVER)
public class KillCommandProcessor extends IRCCommandProcessor{
		
	public KillCommandProcessor(IRCClientController ircClientController) {
		super(ircClientController);
	}

	@Override
	public void proccess(String command) throws Exception {
		String message = command.replace("KILL", "").trim();
		if(!message.isBlank()) {
			ircClientController.send(message);
		}
		ircClientController.kill();
	}

}
