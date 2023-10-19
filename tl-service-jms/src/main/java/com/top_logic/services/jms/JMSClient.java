/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.services.jms;

import java.io.Closeable;

import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.services.jms.JMSService.DestinationConfig;
import com.top_logic.services.jms.JMSService.MQSystemConfigurator;
import com.top_logic.services.jms.JMSService.Type;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;

/**
 * The parent class for every jms producer and consumer.
 */
public class JMSClient implements Closeable {

	private ConnectionFactory _cf;

	private JMSContext _context;

	private Destination _destination;

	private String _destinationName;

	private MQSystemConfigurator _mqSystemConfig;

	private String _charsetProperty;

	/**
	 * Constructor for every JMSClient (producer and consumer) that initializes the connection
	 * factory.
	 */
	public JMSClient(DestinationConfig config) throws JMSException {
		_destinationName = config.getName();

		_mqSystemConfig = TypedConfigUtil.createInstance(config.getMQSystemConfigurator());
		_cf = _mqSystemConfig.setupMQConnection(config.getUser(), config.getPassword());

		_charsetProperty = _mqSystemConfig.getCharsetProperty();

		// Create JMS objects
		_context = _cf.createContext(config.getUser(), config.getPassword());
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

	/**
	 * This method returns the name of the destination config.
	 * 
	 * @return the name of the destination config
	 */
	protected String getDestinationName() {
		return _destinationName;
	}

	/**
	 * This method returns the name of the charset property for the given Message Queue System.
	 * 
	 * @return the name of the charset property
	 */
	public String getCharsetProperty() {
		return _charsetProperty;
	}

	@Override
	public void close() {
		_context.close();
	}

}
