package com.top_logic.services.jms;

import javax.jms.JMSConsumer;
import javax.jms.JMSException;

import com.top_logic.services.jms.JMSService.TargetQueueConfig;

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
	public Consumer(TargetQueueConfig config) throws JMSException {
		super(config);
		_consumer = getContext().createConsumer(getDestination());
	}

	/**
	 * Fetches a message from the given queue
	 */
	public void receive() {
		String message = _consumer.receiveBody(String.class, 15000);
		System.out.println("\nReceived message:\n" + message);
		getContext().close();
	}
}
