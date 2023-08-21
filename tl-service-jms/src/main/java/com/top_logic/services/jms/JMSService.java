package com.top_logic.services.jms;

import java.util.Map;

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
		TargetQueueConfig.QUEUE_MANAGER,
		TargetQueueConfig.CHANNEL,
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

	public JMSService(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();
		getConfig().getTargetQueueConfigs();
	}

	@Override
	protected void shutDown() {

		super.shutDown();
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
