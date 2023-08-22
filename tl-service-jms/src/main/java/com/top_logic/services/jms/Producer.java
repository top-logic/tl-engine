package com.top_logic.services.jms;

import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.TextMessage;

import com.top_logic.services.jms.JMSService.TargetQueueConfig;

public class Producer extends JMSClient {
	
	private JMSProducer _producer;
	
	public Producer(TargetQueueConfig config) throws JMSException {
		super(config);
		_producer = getContext().createProducer();
	}

	public void send(String message) {
		TextMessage _message = getContext().createTextMessage(message);
		_producer.send(getDestination(), _message);
		System.out.println("Sent message:\n" + _message);
	}
}
