package com.top_logic.services.jms.ibmmq;

import com.ibm.msg.client.jakarta.jms.JmsConnectionFactory;
import com.ibm.msg.client.jakarta.jms.JmsFactoryFactory;
import com.ibm.msg.client.jakarta.wmq.WMQConstants;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.services.jms.JMSService.MQSystemConfigurator;
import com.top_logic.util.Resources;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;

/**
 * 
 */
public class IBMMQConfigurator extends AbstractConfiguredInstance<IBMMQConfigurator.Config<?>> implements MQSystemConfigurator {

	/**
	 * Configuration options for {@link IBMMQConfigurator}.
	 */
	public interface Config<I extends IBMMQConfigurator> extends PolymorphicConfiguration<I> {

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
	 * Creates a {@link IBMMQConfigurator} from configuration.
	 * 
	 * @param config
	 *        The configuration.
	 */
	public IBMMQConfigurator(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	/**
	 * Setup for a connection with an IBM MQ.
	 * 
	 * // * @param config // * The config for the destination of the connection
	 * 
	 * @throws JMSException
	 *         Exception if something is not jms conform
	 * @return the connection factory
	 */
	@Override
	public ConnectionFactory setupMQConnection(String un, String pw) throws JMSException {
		Config<?> config = getConfig();
		JmsFactoryFactory ibmff;
		JmsConnectionFactory ibmcf;
		ibmff = JmsFactoryFactory.getInstance(WMQConstants.JAKARTA_WMQ_PROVIDER);
		ibmcf = ibmff.createConnectionFactory();

		// Set properties for the connection
		ibmcf.setStringProperty(WMQConstants.WMQ_HOST_NAME, config.getHost());
		ibmcf.setIntProperty(WMQConstants.WMQ_PORT, config.getPort());
		ibmcf.setStringProperty(WMQConstants.WMQ_CHANNEL, config.getChannel());
		ibmcf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
		ibmcf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, config.getQueueManager());
		ibmcf.setStringProperty(WMQConstants.WMQ_APPLICATIONNAME,
			Resources.getSystemInstance().getString(com.top_logic.layout.I18NConstants.APPLICATION_TITLE));
		ibmcf.setStringProperty(WMQConstants.USERID, un);
		ibmcf.setStringProperty(WMQConstants.PASSWORD, pw);
		return ibmcf;
	}

	@Override
	public String getCharsetProperty() {
		return WMQConstants.JMS_IBM_CHARACTER_SET;
	}
}
