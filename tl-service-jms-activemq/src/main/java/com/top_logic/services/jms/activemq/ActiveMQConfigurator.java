package com.top_logic.services.jms.activemq;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.services.jms.JMSService.MQSystemConfigurator;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;

/**
 * 
 */
public class ActiveMQConfigurator extends AbstractConfiguredInstance<ActiveMQConfigurator.Config<?>> implements MQSystemConfigurator {

	/**
	 * Configuration options for {@link ActiveMQConfigurator}.
	 */
	public interface Config<I extends ActiveMQConfigurator> extends PolymorphicConfiguration<I> {

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
	 * Creates a {@link ActiveMQConfigurator} from configuration.
	 * 
	 * @param config
	 *        The configuration.
	 */
	public ActiveMQConfigurator(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public ConnectionFactory setupMQConnection(String un, String pw) throws JMSException {
		Config<?> config = getConfig();
		String url = config.getURLScheme().toString().toLowerCase() + "://" + config.getHost() + ":" + config.getPort();
		ActiveMQConnectionFactory amqcf = new ActiveMQConnectionFactory(url);
		return amqcf;
	}
}
