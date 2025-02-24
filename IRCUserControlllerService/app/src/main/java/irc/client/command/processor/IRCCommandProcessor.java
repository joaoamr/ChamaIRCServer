package irc.client.command.processor;

import irc.client.controller.IRCClientController;

public abstract class IRCCommandProcessor {
	
	protected IRCClientController ircClientController;
	
	public IRCCommandProcessor(IRCClientController ircClientController) {
		this.ircClientController = ircClientController;
	}
	
	public abstract void proccess(String command) throws Exception;
		
}
