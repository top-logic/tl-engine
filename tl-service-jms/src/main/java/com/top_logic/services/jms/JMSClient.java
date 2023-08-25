/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.services.jms;

import java.io.Closeable;

import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

import com.top_logic.services.jms.JMSService.DestinationConfig;
import com.top_logic.services.jms.JMSService.Type;
import com.top_logic.util.Resources;

/**
 * The parent class for every jms producer and consumer
 */
public class JMSClient implements Closeable {

	private JmsFactoryFactory _ff;

	private JmsConnectionFactory _cf;

	private JMSContext _context;

	private Destination _destination;

	/**
	 * Constructor for every JMSClient (producer and consumer) that initializes the connection
	 * factory.
	 */
	public JMSClient(DestinationConfig config) throws JMSException {
		_ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
		_cf = _ff.createConnectionFactory();
		// Set the properties
		_cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, config.getHost());
		_cf.setIntProperty(WMQConstants.WMQ_PORT, config.getPort());
		_cf.setStringProperty(WMQConstants.WMQ_CHANNEL, config.getChannel());
		_cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
		_cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, config.getQueueManager());
		_cf.setStringProperty(WMQConstants.WMQ_APPLICATIONNAME,
			Resources.getSystemInstance().getString(com.top_logic.layout.I18NConstants.APPLICATION_TITLE));
		_cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
		_cf.setStringProperty(WMQConstants.USERID, config.getUser());
		_cf.setStringProperty(WMQConstants.PASSWORD, config.getPassword());

		// Create JMS objects
		_context = _cf.createContext();
		System.out.println(config.getType());
		if (config.getType() == Type.TOPIC) {
			_destination = getContext().createTopic(config.getDestName());
		} else {
			_destination = getContext().createQueue(config.getDestName());
		}
	}

	/**
	 * This method returns the jms context that is used to create producer and consumer.
	 * 
	 * @return the jmscontext
	 */
	protected JMSContext getContext() {
		return _context;
	}

	/**
	 * This method returns the destination of the jms Connection that is either a queue or a topic.
	 * 
	 * @return the destination
	 */
	protected Destination getDestination() {
		return _destination;
	}

	@Override
	public void close() {
		_context.close();
	}

}
