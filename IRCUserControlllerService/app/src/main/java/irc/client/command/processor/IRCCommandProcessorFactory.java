package irc.client.command.processor;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import irc.client.command.processor.impl.UnknownCommandProcessor;
import irc.client.controller.IRCClientController;

public class IRCCommandProcessorFactory {
	
	private static Map<String, Class<? extends IRCCommandProcessor>> clientProcessors;
	private static Map<String, Class<? extends IRCCommandProcessor>> serverProcessors;

	private static boolean started = false;
	
	static {
		start();
	}
	
	@SuppressWarnings("unchecked")
	public static void start() {
		if(started)
			return;
		
		clientProcessors = new HashMap<>();
		serverProcessors = new HashMap<>();
		Configuration config = new ConfigurationBuilder().forPackages("irc");
		Reflections reflections = new Reflections(config);
		Set<Class<?>> classes = reflections.getTypesAnnotatedWith(IRCCommand.class);
		classes.stream().filter(cl -> IRCCommandProcessor.class.isAssignableFrom(cl))
						.forEach(cl -> {
							String token = cl.getAnnotation(IRCCommand.class).token();
							IRCCommandOrigin origin = cl.getAnnotation(IRCCommand.class).origin();
							switch(origin) {
							case SERVER:
								serverProcessors.put(token, (Class<? extends IRCCommandProcessor>) cl);
								break;
							case CLIENT:
								clientProcessors.put(token, (Class<? extends IRCCommandProcessor>) cl);
								break;
							}							
						});
		
		started = true;
	}
	
	public static IRCCommandProcessor getClientProcessor (String command, IRCClientController controller) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
		return getProcessor(command, controller, clientProcessors);
	}
	
	public static IRCCommandProcessor getServerProcessor (String command, IRCClientController controller) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
		return getProcessor(command, controller, serverProcessors);
	}
	
	private static IRCCommandProcessor getProcessor(String command, IRCClientController controller, Map<String, Class<? extends IRCCommandProcessor>> processors) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		IRCCommandProcessor ircCommandProcessor = null;
		if(command == null)
			return new UnknownCommandProcessor(controller);
		
		String[] tokens = command.split("\\s+");
		
		Class<? extends IRCCommandProcessor> cl = processors.get(tokens[0]);
		
		if(cl != null)
			ircCommandProcessor = cl.getConstructor(IRCClientController.class).newInstance(controller);
		
		if(ircCommandProcessor == null)
			ircCommandProcessor = new UnknownCommandProcessor(controller);
		
		return ircCommandProcessor;
		
	}
	
}
