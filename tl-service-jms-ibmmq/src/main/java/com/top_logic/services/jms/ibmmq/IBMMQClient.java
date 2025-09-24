/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.services.jms.ibmmq;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;

import com.ibm.mq.jakarta.jms.MQConnectionFactory;
import com.ibm.msg.client.jakarta.jms.JmsFactoryFactory;
import com.ibm.msg.client.jakarta.wmq.WMQConstants;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.services.jms.JMSClient;
import com.top_logic.util.Resources;

/**
 * Configuration class for a connection to an IMB MQ System.
 * 
 * @author <a href="mailto:simon.haneke@top-logic.com">Simon Haneke</a>
 */
public class IBMMQClient extends JMSClient {

	/**
	 * Configuration options for {@link IBMMQClient}.
	 */
	@DisplayOrder({
		Config.HOST,
		Config.PORT,
		Config.USER,
		Config.PASSWORD,
		Config.AUTO_RECONNECT,
		Config.RECONNECT_TIMEOUT,
		Config.CHANNEL,
		Config.QUEUE_MANAGER,
		Config.PRODUCER_CONFIGS,
		Config.CONSUMER_CONFIGS
	})
	public interface Config<I extends IBMMQClient> extends JMSClient.Config<I> {

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
		 * Configuration name for {@link #getAutoReconnect()}.
		 */
		String AUTO_RECONNECT = "auto-reconnect";

		/**
		 * Configuration name for {@link #getReconnectTimeout()}.
		 */
		String RECONNECT_TIMEOUT = "reconnect-timeout";

		/**
		 * Configuration name for {@link #getChannel()}.
		 */
		String CHANNEL = "channel";

		/**
		 * Configuration name for {@link #getQueueManager()}.
		 */
		String QUEUE_MANAGER = "queue-manager";
		
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
		@IntDefault(1414)
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

		/**
		 * The Option to activate the IBM auto-reconnect.
		 */
		@Name(AUTO_RECONNECT)
		@Label("IBM auto-reconnect")
		@BooleanDefault(false)
		boolean getAutoReconnect();

		/**
		 * The time in MillisFormat that the IBM auto-reconnect tries to reconnect for.
		 */
		@Name(RECONNECT_TIMEOUT)
		@StringDefault("4min")
		@Format(MillisFormat.class)
		long getReconnectTimeout();

		/**
		 * The channel of the target queue.
		 */
		@Mandatory
		@Name(CHANNEL)
		String getChannel();

		/**
		 * The queue manager of the target queue.
		 */
		@Mandatory
		@Name(QUEUE_MANAGER)
		String getQueueManager();
	}

	/**
	 * Creates a {@link IBMMQClient} from configuration.
	 * 
	 * @param config
	 *        The configuration.
	 */
	public IBMMQClient(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	/**
	 * Setup for a connection with an IBM MQ.
	 * 
	 * @throws JMSException
	 *         Exception if something is not JMS conform
	 * @return the connection factory
	 */
	@Override
	public ConnectionFactory setupConnectionFactory() throws JMSException {
		Config<?> config = (Config<?>) getConfig();
		JmsFactoryFactory ibmff;
		MQConnectionFactory ibmcf;
		ibmff = JmsFactoryFactory.getInstance(WMQConstants.JAKARTA_WMQ_PROVIDER);
		ibmcf = (MQConnectionFactory) ibmff.createConnectionFactory();

		// Set properties for the connection
		ibmcf.setStringProperty(WMQConstants.WMQ_HOST_NAME, config.getHost());
		ibmcf.setIntProperty(WMQConstants.WMQ_PORT, config.getPort());
		ibmcf.setStringProperty(WMQConstants.WMQ_CHANNEL, config.getChannel());
		ibmcf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
		ibmcf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, config.getQueueManager());
		ibmcf.setStringProperty(WMQConstants.WMQ_APPLICATIONNAME,
			Resources.getSystemInstance().getString(com.top_logic.layout.I18NConstants.APPLICATION_TITLE));
		ibmcf.setStringProperty(WMQConstants.USERID, config.getUser());
		ibmcf.setStringProperty(WMQConstants.PASSWORD, config.getPassword());
		if (config.getAutoReconnect()) {
			ibmcf.setClientReconnectOptions(WMQConstants.WMQ_CLIENT_RECONNECT_Q_MGR);
			ibmcf.setClientReconnectTimeout((int) config.getReconnectTimeout());
		}
		return ibmcf;
	}

	@Override
	public String getCharsetProperty() {
		return WMQConstants.JMS_IBM_CHARACTER_SET;
	}
}
