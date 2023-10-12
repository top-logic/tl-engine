package com.top_logic.services.jms;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.services.jms.JMSService.MQSystemConfig;
import com.top_logic.util.Resources;

public class IBMMQConfig extends AbstractConfiguredInstance<IBMMQConfig.Config<?>> implements MQSystemConfig {

	/**
	 * Configuration options for {@link IBMMQConfig}.
	 */
	public interface Config<I extends IBMMQConfig> extends PolymorphicConfiguration<I> {

		/**
		 * Configuration name for {@link #getHost()}.
		 */
		String HOST = "host";

		/**
		 * Configuration name for {@link #getPort()}.
		 */
		String PORT = "port";

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
	 * Creates a {@link IBMMQConfig} from configuration.
	 * 
	 * @param config
	 *        The configuration.
	 */
	public IBMMQConfig(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public ConnectionFactory setupMQConnection() throws JMSException {
		Config<?> config = getConfig();
		JmsFactoryFactory ibmff;
		JmsConnectionFactory ibmcf;
		ibmff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
		ibmcf = ibmff.createConnectionFactory();

		// Set properties for the connection
		ibmcf.setStringProperty(WMQConstants.WMQ_HOST_NAME, config.getHost());
		ibmcf.setIntProperty(WMQConstants.WMQ_PORT, config.getPort());
		ibmcf.setStringProperty(WMQConstants.WMQ_CHANNEL, config.getChannel());
		ibmcf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
		ibmcf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, config.getQueueManager());
		ibmcf.setStringProperty(WMQConstants.WMQ_APPLICATIONNAME,
			Resources.getSystemInstance().getString(com.top_logic.layout.I18NConstants.APPLICATION_TITLE));
//		_ibmcf.setStringProperty(WMQConstants.USERID, config.getUser());
//		_ibmcf.setStringProperty(WMQConstants.PASSWORD, config.getPassword());
		return ibmcf;
	}
}
