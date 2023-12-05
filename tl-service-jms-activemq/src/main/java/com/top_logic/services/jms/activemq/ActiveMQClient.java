/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.services.jms.activemq;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.services.jms.JMSClient;

import jakarta.jms.ConnectionFactory;

/**
 * Configuration class for a connection to an ActiveMQ MQ System.
 * 
 * @author <a href="mailto:simon.haneke@top-logic.com">Simon Haneke</a>
 */
public class ActiveMQClient extends JMSClient {

	/**
	 * Configuration options for {@link ActiveMQClient}.
	 */
	@DisplayOrder({ Config.URL_SCHEME,
		Config.HOST,
		Config.PORT,
		Config.USER,
		Config.PASSWORD,
		Config.PRODUCER_CONFIGS,
		Config.CONSUMER_CONFIGS })
	public interface Config<I extends ActiveMQClient> extends JMSClient.Config<I> {

		/**
		 * Configuration name for {@link #getURLScheme()}.
		 */
		String URL_SCHEME = "url-scheme";

		/**
		 * Configuration name for {@link #getHost()}.
		 */
		String HOST = "host";

		/**
		 * Configuration name for {@link #getPort()}.
		 */
		String PORT = "port";

		/**
		 * Configuration name for {@link #getUser()}.
		 */
		String USER = "user";

		/**
		 * Configuration name for {@link #getPassword()}.
		 */
		String PASSWORD = "password";

		/**
		 * The URL-Scheme of the connection.
		 */
		@Mandatory
		@Name(URL_SCHEME)
		URLScheme getURLScheme();

		/**
		 * The host of the target queue.
		 */
		@Mandatory
		@Name(HOST)
		String getHost();

		/**
		 * The port of the target queue.
		 */
		@Name(PORT)
		@IntDefault(-1)
		int getPort();

		/**
		 * The user name to log in to the message queue server.
		 */
		@Name(USER)
		String getUser();

		/**
		 * The password to the given user name.
		 */
		@Encrypted
		@Name(PASSWORD)
		String getPassword();
	}

	/**
	 * The type of the destination, that can be a queue or a topic.
	 */
	public enum URLScheme {
		/**
		 * 
		 */
		TCP,

		/**
		 * 
		 */
		UDP,

		/**
		 * 
		 */
		VM,

		/**
		 * 
		 */
		JGROUPS;
	}

	/**
	 * Creates a {@link ActiveMQClient} from configuration.
	 * 
	 * @param config
	 *        The configuration.
	 */
	public ActiveMQClient(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	/**
	 * Setup for a connection with an ActiveMQ.
	 * 
	 * @return the connection factory
	 */
	@Override
	public ConnectionFactory setupConnectionFactory() {
		Config<?> config = (Config<?>) getConfig();
		String url = config.getURLScheme().toString().toLowerCase() + "://" + config.getHost() + ":" + config.getPort();
		ActiveMQConnectionFactory amqcf = new ActiveMQConnectionFactory(url);
		amqcf.setUser(config.getUser());
		amqcf.setPassword(config.getPassword());
		return amqcf;
	}
}
