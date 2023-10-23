/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.services.jms;

import java.io.Closeable;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;

import jakarta.jms.Destination;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.Message;
import jakarta.jms.Topic;

/**
 * Base class for a jms consumer (fetches messages from a queue).
 * 
 * @author <a href="mailto:sha@top-logic.com">Simon Haneke</a>
 */
public abstract class Consumer extends AbstractConfiguredInstance<Consumer.Config<?>> implements Closeable {

	private JMSConsumer _consumer;

	private String _name;

	/**
	 * Configuration options for {@link Consumer}.
	 */
	public interface Config<I extends Consumer> extends ClientConfig<I> {
		/**
		 * Implemented / extended by each unique consumer type.
		 */
	}

	/**
	 * Constructor for a consumer that sets the config.
	 * 
	 * @param config
	 *        The config for the connection to the queue
	 */
	public Consumer(InstantiationContext instContext, Config<?> config) {
		super(instContext, config);
		_name = config.getName();
	}

	/**
	 * Creates a {@link JMSConsumer} on an existing {@link JMSContext} containing the connection to
	 * a message queue system.
	 * 
	 * @param client
	 *        The Object establishing the connection and holding the {@link JMSContext}.
	 */
	public void setup(JMSClient client) {
		Config.Type type = getConfig().getType();
		String destName = getConfig().getDestName();
		Destination destination = client.createDestination(type, destName);
		if (type == Config.Type.TOPIC) {
			_consumer = client.getContext().createSharedDurableConsumer((Topic) destination, destName);
		} else {
			_consumer = client.getContext().createConsumer(destination);
		}
	}

	/**
	 * Fetches a message from the given queue.
	 */
	public void receive() {
		try {
			while (true) {
				Message message = _consumer.receive();
				processMessage(message);
			}
		} finally {
			Logger.info("Stopping consumer " + _name + ".", Consumer.class);
			_consumer.close();
		}
	}

	/**
	 * Processes the received Message.
	 * 
	 * @param message
	 *        The message to be processed
	 */
	public abstract void processMessage(Message message);

	@Override
	public void close() {
		_consumer.close();
	}
}
