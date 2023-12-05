/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.services.jms;

import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.event.infoservice.InfoService;

import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;

/**
 * The TopLogic Service to set the config for a connection and establish this connection to a JMS
 * Message System. In this case the connection is tuned to the IBM MQ system.
 * 
 * @author <a href="mailto:simon.haneke@top-logic.com">Simon Haneke</a>
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
		DestinationConfig.MQ_SYSTEM_CLIENT })
	public interface DestinationConfig extends NamedConfigMandatory {
		/**
		 * Configuration name for {@link #getMQSystemClient()}
		 */
		String MQ_SYSTEM_CLIENT = "mq-system-client";

		/**
		 * The configurator for the Message Queue System that is being used.
		 */
		@Mandatory
		@Name(MQ_SYSTEM_CLIENT)
		@ImplementationClassDefault(JNDIMQClient.class)
		PolymorphicConfiguration<MQSystemClient> getMQSystemClient();
	}

	/**
	 * The configuration for the system behind the message queue that runs the queue manager.
	 */
	public interface MQSystemClient {
		/**
		 * Creates a {@link JMSContext} and stores it to be accessible for creation of
		 * {@link Producer}s and {@link Consumer}s.
		 */
		public void setupMQConnection() throws JMSException, NamingException;

		/**
		 * Should be overwritten if the MQ System defines a custom charset property, so it returns
		 * the name of it.
		 */
		default String getCharsetProperty() {
			return null;
		}

		/**
		 * A map of all created producer configurations for the MQ System.
		 * 
		 * @return a map of String name to {@link Producer.Config}
		 */
		public Map<String, Producer.Config<?>> getProducerConfigs();

		/**
		 * A map of all created consumer configurations for the MQ System.
		 * 
		 * @return a map of String name to {@link Consumer.Config}
		 */
		public Map<String, Consumer.Config<?>> getConsumerConfigs();
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
			JMSClient mqClient = (JMSClient) TypedConfigUtil.createInstance(config.getMQSystemClient());
			try {
				mqClient.setupMQConnection();

				for (Producer.Config<?> pconfig : mqClient.getProducerConfigs().values()) {
					Producer prod = TypedConfigUtil.createInstance(pconfig);
					prod.setup(mqClient);
					_producers.put(pconfig.getName(), prod);
				}
				for (Consumer.Config<?> cconfig : mqClient.getConsumerConfigs().values()) {
					Consumer cons = TypedConfigUtil.createInstance(cconfig);
					cons.setup(mqClient);
					Thread consThread = new Thread(() -> cons.receive());
					consThread.setName("JMS Consumer - " + cconfig.getName());
					consThread.start();
					_consumers.put(cconfig.getName(), new Pair<>(cons, consThread));
				}
			} catch (JMSException ex) {
				InfoService.logError(I18NConstants.ERROR_ESTABLISH_CONNECTION__NAME.fill(config.getName()),
					ex.getMessage(), ex, JMSService.class);
			} catch (NamingException ex) {
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
		_producers.clear();
		_producers = null;
		for (Pair<Consumer, Thread> pair : _consumers.values()) {
			Consumer cons = pair.getFirst();
			Thread thread = pair.getSecond();
			thread.interrupt();
			Logger.info("Interrupted consumer thread " + thread.getName() + ".", JMSService.class);
			cons.close();
		}
		_consumers.clear();
		_consumers = null;
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
