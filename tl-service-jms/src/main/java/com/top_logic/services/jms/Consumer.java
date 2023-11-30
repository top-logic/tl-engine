/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.services.jms;

import java.io.Closeable;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.thread.ThreadContextManager;

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
public abstract class Consumer<C extends Consumer.Config<?>> extends AbstractConfiguredInstance<C>
		implements Closeable {

	private JMSConsumer _consumer;

	private String _name;

	private volatile boolean _closed = false;

	/**
	 * Configuration options for {@link Consumer}.
	 */
	public interface Config<I extends Consumer<?>> extends ClientConfig<I> {
		/**
		 * Implemented / extended by each unique consumer type.
		 * 
		 * @see com.top_logic.services.jms.ConsumerByExpression
		 */
	}

	/**
	 * Constructor for a consumer that sets the config.
	 * 
	 * @param config
	 *        The config for the connection to the queue
	 */
	public Consumer(InstantiationContext instContext, C config) {
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
		while (!_closed) {
			try {
				Message message = _consumer.receive();
				ThreadContextManager.inInteraction(Consumer.class.getName(), () -> processMessage(message));
			} catch (Throwable ex) {
				if (_closed) {
					return;
				}
				Logger.error(I18NConstants.ERROR_RECEIVING_MSG__NAME.fill(_name) + " " + ex.getMessage(), ex, this);
				if (ex instanceof InterruptedException || ex instanceof ThreadDeath) {
					Logger.info("Stopping consumer " + _name + ".", Consumer.class);
					close();
					return;
				}
			}
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
		_closed = true;
		_consumer.close();
	}
}
