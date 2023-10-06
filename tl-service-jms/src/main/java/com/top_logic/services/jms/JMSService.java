/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
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
 * The TopLogic Service to set the config for a connection and establish this connection to a JMS
 * Message System. In this case the connection is tuned to the IBM MQ system.
 */
public class JMSService extends ConfiguredManagedClass<JMSService.Config> {

	/**
	 * Interface for the Service, that collects all destination configurations to establish
	 * connactions.
	 */
	public interface Config extends ConfiguredManagedClass.Config<JMSService> {
		/**
		 * A map of all created destination configurations.
		 * 
		 * @return a map of String name to {@link DestinationConfig}
		 */
		@Key(DestinationConfig.NAME_ATTRIBUTE)
		Map<String, DestinationConfig> getDestinationConfigs();
	}

	/**
	 * A {@link DestinationConfig} contains all relevant properties that are needed for a
	 * connection.
	 */
	@DisplayOrder({ DestinationConfig.NAME_ATTRIBUTE,
		DestinationConfig.HOST,
		DestinationConfig.PORT,
		DestinationConfig.CHANNEL,
		DestinationConfig.QUEUE_MANAGER,
		DestinationConfig.DEST_NAME,
		DestinationConfig.TYPE,
		DestinationConfig.USER,
		DestinationConfig.PASSWORD })
	public interface DestinationConfig extends NamedConfigMandatory {

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
		 * Configuration name for {@link #getDestName()}.
		 */
		String DEST_NAME = "dest-name";

		/**
		 * Configuration name for {@link #getType()}
		 */
		String TYPE = "type";

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
		 * The name that is the destination of the connection.
		 */
		@Name(DEST_NAME)
		@Mandatory
		String getDestName();

		/**
		 * The type the destination of the connection has.
		 */
		@Name(TYPE)
		Type getType();
	}

	/**
	 * The type of the destination, that can be a queue or a topic.
	 */
	public enum Type {
		/**
		 * A queue is a point-to-point connection between a producer and a consumer.
		 */
		QUEUE,

		/**
		 * A topic is a publish/subscribe connection so multiple subscribed consumers can receive
		 * messages simultaneously from a topic.
		 */
		TOPIC;
	}

	private Map<String, Producer> _producers = new HashMap<>();

	private Map<String, Consumer> _consumers = new HashMap<>();

	/**
	 * Constructor for the service that establishes connections with the given config.
	 * 
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
		for (DestinationConfig config : getConfig().getDestinationConfigs().values()) {
			try {
				Producer prod = new Producer(config);
				_producers.put(config.getName(), prod);
				Consumer cons = new Consumer(config);
				_consumers.put(config.getName(), cons);
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
	 * Gets and returns the producer from all producers for the given name.
	 * 
	 * @param name
	 *        Name of the Producer
	 * @return The requested Producer
	 */
	public Producer getProducer(String name) {
		return _producers.get(name);
	}

	/**
	 * Gets and returns the consumer from all consumers for the given name.
	 * 
	 * @param name
	 *        Name of the Consumer
	 * @return The requested Consumer
	 */
	public Consumer getConsumer(String name) {
		return _consumers.get(name);
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
