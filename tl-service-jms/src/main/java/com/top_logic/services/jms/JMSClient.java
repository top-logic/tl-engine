/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.services.jms;

import java.io.Closeable;
import java.util.Map;

import javax.naming.NamingException;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.services.jms.JMSService.MQSystemClient;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;

/**
 * The parent class for every Message Queue System Client.
 * 
 * @author <a href="mailto:simon.haneke@top-logic.com">Simon Haneke</a>
 */
public abstract class JMSClient extends AbstractConfiguredInstance<JMSClient.Config<?>>
		implements MQSystemClient, Closeable {

	private JMSContext _context;

	private Destination _destination;

	/**
	 * Configuration options for {@link JMSClient}.
	 */
	public interface Config<I extends JMSClient> extends PolymorphicConfiguration<I> {
		/**
		 * Configuration name for {@link #getProducerConfigs()}.
		 */
		String PRODUCER_CONFIGS = "producer-configs";

		/**
		 * Configuration name for {@link #getConsumerConfigs()}.
		 */
		String CONSUMER_CONFIGS = "consumer-configs";

		/**
		 * A map of all created producer configurations.
		 * 
		 * @return a map of String name to {@link Producer.Config}
		 */
		@Name(PRODUCER_CONFIGS)
		@Key(Producer.Config.NAME_ATTRIBUTE)
		Map<String, Producer.Config<?>> getProducerConfigs();
		
		/**
		 * A map of all created consumer configurations.
		 * 
		 * @return a map of String name to {@link Consumer.Config}
		 */
		@Name(CONSUMER_CONFIGS)
		@Key(Consumer.Config.NAME_ATTRIBUTE)
		Map<String, Consumer.Config<?>> getConsumerConfigs();
	}

	/**
	 * Constructor for each JMSClient that represents a connection to an MQ System.
	 */
	public JMSClient(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	/**
	 * This method returns the {@link JMSContext} that is used to create producer and consumer.
	 * 
	 * @return the JMSContext
	 */
	protected JMSContext getContext() {
		return _context;
	}


	/**
	 * This method returns the destination of the JMS Connection that is either a queue or a topic.
	 * 
	 * @param type
	 *        The type of the destination (queue or topic).
	 * @param destName
	 *        The name of the destination (queue name or topic name).
	 * @return the destination
	 */
	protected Destination createDestination(ClientConfig.Type type, String destName) {
		if (type == ClientConfig.Type.TOPIC) {
			_destination = getContext().createTopic(destName);
		} else {
			_destination = getContext().createQueue(destName);
		}
		return _destination;
	}

	@Override
	public void close() {
		_context.close();
	}

	/**
	 * This method creates a {@link JMSContext} and stores it to be accessible for creation of
	 * {@link Producer}s and {@link Consumer}s.
	 * 
	 * @throws JMSException
	 *         Exception if something is not JMS conform
	 * @throws NamingException
	 *         Exception if something went wrong with the JNDI lookup
	 */
	@Override
	public void setupMQConnection() throws JMSException, NamingException {
		_context = setupConnectionFactory().createContext();
	}

	/**
	 * This method creates and returns the {@link ConnectionFactory} that builds and holds the
	 * connection to a MQ System.
	 * 
	 * @throws JMSException
	 *         Exception if something is not JMS conform
	 * @throws NamingException
	 *         Exception if something went wrong with the JNDI lookup
	 * @return the <code>ConnectionFactory</code>
	 */
	abstract protected ConnectionFactory setupConnectionFactory() throws JMSException, NamingException;

	@Override
	public Map<String, Producer.Config<?>> getProducerConfigs() {
		return getConfig().getProducerConfigs();
	}

	@Override
	public Map<String, Consumer.Config<?>> getConsumerConfigs() {
		return getConfig().getConsumerConfigs();
	}

}
