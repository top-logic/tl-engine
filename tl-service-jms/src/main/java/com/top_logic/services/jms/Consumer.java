/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.services.jms;

import java.io.UnsupportedEncodingException;

import javax.jms.BytesMessage;
import javax.jms.JMSConsumer;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.Topic;

import com.ibm.msg.client.wmq.WMQConstants;

import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.services.jms.JMSService.DestinationConfig;
import com.top_logic.services.jms.JMSService.MessageProcessor;
import com.top_logic.services.jms.JMSService.Type;

/**
 * Class for a jms consumer (fetches messages from a queue).
 */
public class Consumer extends JMSClient {

	private JMSConsumer _consumer;

	private MessageProcessor _processor;

	/**
	 * @param config
	 *        The config for the connection to the queue
	 * @throws JMSException
	 *         Exception if something is not jms conform
	 */
	public Consumer(DestinationConfig config) throws JMSException {
		super(config);
		if (config.getType().equals(Type.TOPIC)) {
			_consumer = getContext().createSharedDurableConsumer((Topic) getDestination(), config.getDestName());
		} else {
			_consumer = getContext().createConsumer(getDestination());
		}
		_processor = TypedConfigUtil.createInstance(config.getProcessor());
	}

	/**
	 * Fetches a message from the given queue
	 */
	public void receive() {
//		while (true) {
		Message message = _consumer.receive();
		_processor.processMessage(message);

		String msg = "";
		try {
			if (message instanceof TextMessage) {
				msg = message.getBody(String.class);
			} else if (message instanceof BytesMessage) {
				byte[] m = message.getBody(byte[].class);
				String charset = message.getStringProperty(WMQConstants.JMS_IBM_CHARACTER_SET);
				msg = new String(m, charset);
			}
		} catch (JMSException ex) {
			ex.printStackTrace();
		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
		}

		System.out.println("\nReceived message:\n" + msg);
//			getContext().close();
//		}
	}
}
