package com.top_logic.services.jms;

import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.event.infoservice.InfoService;

/**
 * 
 */
public class JMSService extends ConfiguredManagedClass<JMSService.Config> {

	/**
	 * 
	 */
	public interface Config extends ConfiguredManagedClass.Config<JMSService> {
		/**
		 * @return a map of multiple TargetQueueConfigs with their name
		 */
		@Key(TargetQueueConfig.NAME_ATTRIBUTE)
		Map<String, TargetQueueConfig> getTargetQueueConfigs();
	}

	/**
	 * A TargetQueueConfig contains all relevant properties that are needed for a connection
	 */
	@DisplayOrder({ TargetQueueConfig.NAME_ATTRIBUTE,
		TargetQueueConfig.HOST,
		TargetQueueConfig.PORT,
		TargetQueueConfig.CHANNEL,
		TargetQueueConfig.QUEUE_MANAGER,
		TargetQueueConfig.QUEUE_NAME,
		TargetQueueConfig.USER,
		TargetQueueConfig.PASSWORD })
	public interface TargetQueueConfig extends NamedConfigMandatory {

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
		 * Configuration name for {@link #getUser()}.
		 */
		String USER = "user";

		/**
		 * Configuration name for {@link #getPassword()}.
		 */
		String PASSWORD = "password";

		/**
		 * Configuration name for {@link #getQueueName()}.
		 */
		String QUEUE_NAME = "queue-name";

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

		/**
		 * The user name to log in to the message queue server.
		 */
		@Name(USER)
		String getUser();

		/**
		 * The password to the given user name.
		 */
		@Name(PASSWORD)
		@Encrypted
		String getPassword();

		/**
		 * The queue name that is the destination of the connection.
		 */
		@Name(QUEUE_NAME)
		@Mandatory
		String getQueueName();
	}

	private Map<String, Producer> _producers = new HashMap<>();

	/**
	 * @param context
	 *        The context which can be used to instantiate inner configurations.
	 * @param config
	 *        The configuration for the service.
	 */
	public JMSService(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();
		for (TargetQueueConfig config : getConfig().getTargetQueueConfigs().values()) {
			try {
				Producer prod = new Producer(config);
				_producers.put(config.getName(), prod);
			} catch (JMSException ex) {
				InfoService.logError(I18NConstants.ERROR_ESTABLISH_CONNECTION__NAME.fill(config.getName()),
					ex.getMessage(), ex, JMSService.class);
			} catch (RuntimeException ex) {
				InfoService.logError(I18NConstants.ERROR_ESTABLISH_CONNECTION__NAME.fill(config.getName()),
					ex.getMessage(), ex, JMSService.class);
			}
		}
	}

	@Override
	protected void shutDown() {
		for (Producer prod : _producers.values()) {
			prod.close();
		}
		super.shutDown();
	}

	/**
	 * @param name
	 *        Name of the Producer
	 * @return The requested Producer
	 */
	public Producer getProducer(String name) {
		return _producers.get(name);
	}

	/**
	 * Singleton reference for {@link JMSService}.
	 */
	public static class Module extends TypedRuntimeModule<JMSService> {

		/**
		 * Singleton {@link JMSService.Module} instance.
		 */
		public static final JMSService.Module INSTANCE = new JMSService.Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<JMSService> getImplementation() {
			return JMSService.class;
		}
	}
}
