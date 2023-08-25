/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.services.jms;

import javax.jms.JMSConsumer;
import javax.jms.JMSException;
import javax.jms.Topic;

import com.top_logic.services.jms.JMSService.DestinationConfig;
import com.top_logic.services.jms.JMSService.Type;

/**
 * Class for a jms consumer (fetches messages from a queue)
 */
public class Consumer extends JMSClient {

	private JMSConsumer _consumer;

	/**
	 * @param config
	 *        The config for the connection to the queue
	 * @throws JMSException
	 *         Exception if something is not jms conform
	 */
	public Consumer(DestinationConfig config) throws JMSException {
		super(config);
		if (config.getType().equals(Type.TOPIC)) {
			_consumer = getContext().createSharedDurableConsumer((Topic) getDestination(), "FruitsTopic");
		} else {
			_consumer = getContext().createConsumer(getDestination());
		}
	}

	/**
	 * Fetches a message from the given queue
	 */
	public void receive() {
		while (true) {
			String message = _consumer.receiveBody(String.class);
			System.out.println("\nReceived message:\n" + message);
			getContext().close();
		}
	}
}
