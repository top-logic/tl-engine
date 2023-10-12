/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.services.jms;

import java.io.Closeable;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

import com.top_logic.services.jms.JMSService.DestinationConfig;
import com.top_logic.services.jms.JMSService.MQSystem;
import com.top_logic.services.jms.JMSService.Type;
import com.top_logic.util.Resources;

/**
 * The parent class for every jms producer and consumer.
 */
public class JMSClient implements Closeable {

	private ConnectionFactory _cf;

	private JMSContext _context;

	private Destination _destination;

	private String _destinationName;

	private JmsFactoryFactory _ibmff;

	private JmsConnectionFactory _ibmcf;

	/**
	 * Constructor for every JMSClient (producer and consumer) that initializes the connection
	 * factory.
	 */
	public JMSClient(DestinationConfig config) throws JMSException {
		_destinationName = config.getName();
		if (config.getMQSystem() == MQSystem.IBMMQ) {
			_cf = setupIBMMQConnection(config);
		} else if (config.getMQSystem() == MQSystem.ACTIVEMQ) {
			_cf = setupActiveMQConnection(config);
		}

		// Create JMS objects
		_context = _cf.createContext(config.getUser(), config.getPassword());
		if (config.getType() == Type.TOPIC) {
			_destination = getContext().createTopic(config.getDestName());
		} else {
			_destination = getContext().createQueue(config.getDestName());
		}
	}

	/**
	 * Setup for a connection with an IBM MQ.
	 * 
	 * @param config
	 *        The config for the destination of the connection
	 * @throws JMSException
	 *         Exception if something is not jms conform
	 * @return the connection factory
	 */
	private ConnectionFactory setupIBMMQConnection(DestinationConfig config) throws JMSException {
		_ibmff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
		_ibmcf = _ibmff.createConnectionFactory();

		// Set properties for the connection
		_ibmcf.setStringProperty(WMQConstants.WMQ_HOST_NAME, config.getHost());
		_ibmcf.setIntProperty(WMQConstants.WMQ_PORT, config.getPort());
		_ibmcf.setStringProperty(WMQConstants.WMQ_CHANNEL, config.getChannel());
		_ibmcf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
		_ibmcf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, config.getQueueManager());
		_ibmcf.setStringProperty(WMQConstants.WMQ_APPLICATIONNAME,
			Resources.getSystemInstance().getString(com.top_logic.layout.I18NConstants.APPLICATION_TITLE));
//		_ibmcf.setStringProperty(WMQConstants.USERID, config.getUser());
//		_ibmcf.setStringProperty(WMQConstants.PASSWORD, config.getPassword());
		return _ibmcf;
	}

	/**
	 * Setup for a connection with an ActiveMQ by Apache.
	 * 
	 * @param config
	 *        The config for the destination of the connection
	 * 
	 * @return the connection factory
	 */
	private ConnectionFactory setupActiveMQConnection(DestinationConfig config) {
		// TODO Auto-generated method stub
		return null;
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

	@Override
	public void close() {
		_context.close();
	}

}
