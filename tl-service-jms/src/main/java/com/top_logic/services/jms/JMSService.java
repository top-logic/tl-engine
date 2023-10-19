/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.services.jms;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.event.infoservice.InfoService;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Message;

/**
 * The TopLogic Service to set the config for a connection and establish this connection to a JMS
 * Message System. In this case the connection is tuned to the IBM MQ system.
 * 
 * @author <a href="mailto:sha@top-logic.com">Simon Haneke</a>
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
		DestinationConfig.DEST_NAME,
		DestinationConfig.TYPE,
		DestinationConfig.USER,
		DestinationConfig.PASSWORD,
		DestinationConfig.MQ_SYSTEM_CONFIGURATOR,
		DestinationConfig.MESSAGE_PROCESSOR })
	public interface DestinationConfig extends NamedConfigMandatory {

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
		 * Configuration name for {@link #getMQSystemConfigurator()}
		 */
		String MQ_SYSTEM_CONFIGURATOR = "mq-system-configurator";

		/**
		 * Configuration name for {@link #getProcessor()}
		 */
		String MESSAGE_PROCESSOR = "message-processor";

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
		 * The name that is the destination of the connection.
		 */
		@Mandatory
		@Name(DEST_NAME)
		String getDestName();

		/**
		 * The type the destination of the connection has.
		 */
		@Name(TYPE)
		Type getType();

		/**
		 * The configurator for the Message Queue System that is being used.
		 */
		@Mandatory
		@Name(MQ_SYSTEM_CONFIGURATOR)
		@ImplementationClassDefault(JNDIMQConfigurator.class)
		PolymorphicConfiguration<MQSystemConfigurator> getMQSystemConfigurator();

		/**
		 * The config for a processor that processes messages.
		 */
		@Name(MESSAGE_PROCESSOR)
		PolymorphicConfiguration<MessageProcessor> getProcessor();
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

	/**
	 * The configuration for the System behind the Message Queue that runs the Queue Manager.
	 */
	public interface MQSystemConfigurator {
		/**
		 * 
		 */
		public ConnectionFactory setupMQConnection(String un, String pw) throws JMSException;

		/**
		 * 
		 */
		default String getCharsetProperty() {
			return "charset";
		}
	}

	/**
	 * A {@link MessageProcessor} provides a method to process messages that where received by a
	 * {@link Consumer}.
	 * 
	 * Implementations of {@link MessageProcessor} typically contain a config to set a custom
	 * processor for received messages.
	 */
	public interface MessageProcessor {
		/**
		 * Processes the given {@link Message}.
		 * 
		 * @param message
		 *        The {@link Message} received by the {@link Consumer}.
		 */
		public void processMessage(Message message);
	}

	private Map<String, Producer> _producers = new HashMap<>();

	private Map<String, Pair<Consumer, Thread>> _consumers = new HashMap<>();

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
				if (config.getProcessor() != null) {
					Consumer cons = new Consumer(config);

					Thread consThread = new Thread(() -> cons.receive());
					consThread.setName("JMS Consumer - " + config.getName());
					consThread.start();
					_consumers.put(config.getName(), new Pair<>(cons, consThread));
				}
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
		for (Pair<Consumer, Thread> pair : _consumers.values()) {
			Consumer cons = pair.getFirst();
			Thread thread = pair.getSecond();
			thread.interrupt();
			Logger.info("Interrupted consumer thread " + thread.getName() + ".", JMSService.class);
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
		return _consumers.get(name).getFirst();
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
