package com.top_logic.services.jms;

import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;

import com.top_logic.basic.Logger;
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

public class JMSService extends ConfiguredManagedClass<JMSService.Config> {

	public interface Config extends ConfiguredManagedClass.Config<JMSService> {
		@Key(TargetQueueConfig.NAME_ATTRIBUTE)
		Map<String, TargetQueueConfig> getTargetQueueConfigs();
	}

	@DisplayOrder({ TargetQueueConfig.NAME_ATTRIBUTE,
		TargetQueueConfig.HOST,
		TargetQueueConfig.PORT,
		TargetQueueConfig.CHANNEL,
		TargetQueueConfig.QUEUE_MANAGER,
		TargetQueueConfig.QUEUE_NAME,
		TargetQueueConfig.USER,
		TargetQueueConfig.PASSWORD })
	public interface TargetQueueConfig extends NamedConfigMandatory {

		String HOST = "host";

		String PORT = "port";

		String CHANNEL = "channel";

		String QUEUE_MANAGER = "queue-manager";

		String USER = "user";

		String PASSWORD = "password";

		String QUEUE_NAME = "queue-name";

		@Mandatory
		@Name(HOST)
		String getHost();

		@Name(PORT)
		@IntDefault(1414)
		int getPort();

		@Mandatory
		@Name(CHANNEL)
		String getChannel();

		@Mandatory
		@Name(QUEUE_MANAGER)
		String getQueueManager();

		@Name(USER)
		String getUser();

		@Name(PASSWORD)
		@Encrypted
		String getPassword();

		@Name(QUEUE_NAME)
		@Mandatory
		String getQueueName();
	}

	private Map<String, Producer> _producers = new HashMap<>();

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
				Logger.error("Unable to create Producer: " + config, ex, JMSService.class);
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
