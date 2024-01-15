/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.services.jms;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.order.DisplayOrder;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;

/**
 * Configuration class for a connection to a MQ System defined in JNDI.
 * 
 * @author <a href="mailto:simon.haneke@top-logic.com">Simon Haneke</a>
 */
public class JNDIMQClient extends JMSClient {

	/**
	 * Configuration options for {@link JNDIMQClient}.
	 */
	@DisplayOrder({
		Config.CONNECTION_FACTORY,
		Config.PRODUCER_CONFIGS,
		Config.CONSUMER_CONFIGS
	})
	public interface Config<I extends JNDIMQClient> extends JMSClient.Config<I> {
		/**
		 * Configuration name for {@link #getConnectionFactory()}.
		 */
		String CONNECTION_FACTORY = "connection-factory";

		/**
		 * The JNDI name of the <code>ConnectionFactory</code>.
		 */
		@Mandatory
		@Name(CONNECTION_FACTORY)
		String getConnectionFactory();
	}

	/**
	 * Creates a {@link JNDIMQClient} from configuration.
	 * 
	 * @param config
	 *        The configuration.
	 */
	public JNDIMQClient(InstantiationContext context, Config<?> config) {
		super(context, config);
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
	@Override
	protected Destination createDestination(ClientConfig.Type type, String destName) {
		try {
			return InitialContext.doLookup(destName);
		} catch (NamingException ex) {
			return super.createDestination(type, destName);
		}
	}

	/**
	 * Setup for a connection defined in JNDI.
	 * 
	 * @throws NamingException
	 *         Exception if something went wrong with the JNDI lookup
	 */
	@Override
	public ConnectionFactory setupConnectionFactory() throws NamingException {
		Config<?> config = (Config<?>) getConfig();
		InitialContext jndi = new InitialContext();
		return (ConnectionFactory) jndi.lookup(config.getConnectionFactory());
	}
}
