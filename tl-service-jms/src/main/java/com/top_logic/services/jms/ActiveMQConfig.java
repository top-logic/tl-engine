package com.top_logic.services.jms;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.services.jms.JMSService.MQSystemConfig;

public class ActiveMQConfig extends AbstractConfiguredInstance<ActiveMQConfig.Config<?>> implements MQSystemConfig {

	/**
	 * Configuration options for {@link ActiveMQConfig}.
	 */
	public interface Config<I extends ActiveMQConfig> extends PolymorphicConfiguration<I> {

		/**
		 * Configuration name for {@link #getHost()}.
		 */
		String HOST = "host";

		/**
		 * Configuration name for {@link #getPort()}.
		 */
		String PORT = "port";

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
	}

	/**
	 * Creates a {@link ActiveMQConfig} from configuration.
	 * 
	 * @param config
	 *        The configuration.
	 */
	public ActiveMQConfig(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public ConnectionFactory setupMQConnection() throws JMSException {
		Config<?> config = getConfig();
//		ActiveMQConnectionFactory amqcf = new ActiveMQConnectionFactory("vm://localhost");
		return null;
	}
}
