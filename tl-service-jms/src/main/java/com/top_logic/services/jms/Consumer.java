package com.top_logic.services.jms;

import javax.jms.JMSConsumer;
import javax.jms.JMSException;

import com.top_logic.services.jms.JMSService.TargetQueueConfig;

public class Consumer extends JMSClient {

	private JMSConsumer _consumer;

	public Consumer(TargetQueueConfig config) throws JMSException {
		super(config);
		_consumer = getContext().createConsumer(getDestination());
	}

	public void receive() {
		String message = _consumer.receiveBody(String.class, 15000);
		System.out.println("\nReceived message:\n" + message);
		getContext().close();
	}
}
