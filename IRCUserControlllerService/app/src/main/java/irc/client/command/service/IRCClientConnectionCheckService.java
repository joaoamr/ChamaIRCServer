package irc.client.command.service;

import java.util.Calendar;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import irc.client.controller.IRCClientController;
import irc.server.messaging.kafka.KafkaProducerFactory;
import irc.server.utils.IRCServerConstants;

public class IRCClientConnectionCheckService extends Thread {
	private IRCClientController client;
	private long frequencyTimeInMilis = IRCServerConstants.CONNECTION_CHECK_FREQUENCY;

	public IRCClientConnectionCheckService(IRCClientController client) {
		this.client = client;
	}

	@Override
	public void run() {
		KafkaProducer<String, String> producer = KafkaProducerFactory.getProducer();
		while (client.isAlive()) {
			try {
				Thread.sleep(frequencyTimeInMilis);
			} catch (InterruptedException e) {
			}

			long diff = Calendar.getInstance().getTimeInMillis() - client.getLastMessageTime();
			
			if(!client.isRegisterCompleted()) {
				client.kill();
				continue;
			} else {
				String topic = IRCServerConstants.NICK_LOCK_TOPIC_PREFIX + client.getNick();
				String ts = "ping-" + String.valueOf(Calendar.getInstance().getTimeInMillis());
				ProducerRecord<String, String> record = new ProducerRecord<>(topic, ts);
				producer.send(record);
			}
			
			if (diff > frequencyTimeInMilis) {
				if (diff > (frequencyTimeInMilis * 2)) {
					client.kill();
				} else {
					client.send("PING :TIMEOUTCHECK");
				}
			}
		}
		producer.close();
	}
}
