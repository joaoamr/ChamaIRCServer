package irc.client.command.processor.impl;

import irc.client.command.processor.IRCCommandOrigin;
import irc.client.command.processor.IRCCommandProcessor;
import irc.client.command.processor.IRCCommand;
import irc.client.controller.IRCClientController;

@IRCCommand(token="PING",  origin=IRCCommandOrigin.CLIENT)
public class PingCommandProcessor extends IRCCommandProcessor{
		
	public PingCommandProcessor(IRCClientController ircClientController) {
		super(ircClientController);
	}

	@Override
	public void proccess(String command) throws Exception {
		String response = command.replace("PING", "").trim();
		if(command.charAt(0) == ':') //mIRC ping check
			response = "PONG " + command;
		else { 
			//to avoid xChat log pong response in console, the server will send a ping back to check
			//instead of response the pong
			response = "PING :TIMEOUTCHECK";
		}
		
		ircClientController.send(response);
	}

}
