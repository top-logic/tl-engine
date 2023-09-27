/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.services.consumer;

import static com.top_logic.basic.logging.LogUtil.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.kafka.clients.consumer.KafkaConsumer;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.kafka.services.common.TopicChecker;

/**
 * A service providing access to {@link KafkaConsumer}s.
 * 
 * @author <a href=mailto:wta@top-logic.com>wta</a>
 */
@ServiceDependencies(ThreadContextManager.Module.class)
public class KafkaConsumerService extends ConfiguredManagedClass<KafkaConsumerService.Config> {
	
	/**
	 * Service interface configuration for {@link KafkaConsumerService}.
	 * 
	 * @author <a href="mailto:wta@top-logic.com">wta</a>
	 */
	public interface Config extends ConfiguredManagedClass.Config<KafkaConsumerService> {
		
		/**
		 * a (possibly empty) {@link List} of configured
		 *         {@link ConsumerDispatcher}s
		 */
		@Key(ConsumerDispatcherConfiguration.NAME_ATTRIBUTE)
		List<ConsumerDispatcherConfiguration<?, ?>> getConsumers();
		
		/**
		 * the amount of time (in milliseconds) to wait for a dispatcher
		 *         thread to terminate
		 */
		@LongDefault(5000)
		long getJoinTimeout();
	}

	/**
	 * A {@link List} of registered {@link ConsumerDispatcher}s.
	 */
	private final List<ConsumerDispatcher<?, ?>> _consumers = new ArrayList<>();
	
	/**
	 * Create a {@link KafkaConsumerService}.
	 * 
	 * @param context
	 *            the {@link InstantiationContext} to create the new object in
	 * @param config
	 *            the configuration object to be used for instantiation
	 */
	public KafkaConsumerService(final InstantiationContext context, final Config config) {
		super(context, config);
	}
	
	@Override
	protected void startUp() {
		// call super-implementation first
		super.startUp();
		withBeginEndLogging(KafkaConsumerService.class, "Creating and starting consumers.", this::startUpConsumers);
	}

	/**
	 * Instantiate every {@link ConsumerDispatcher} and {@link Thread#start() start} them.
	 * <p>
	 * This is done in the {@link #startUp()} method in order to support restarting this service.
	 * </p>
	 */
	protected void startUpConsumers() {
		InstantiationContext context = new DefaultInstantiationContext(KafkaConsumerService.class);
		for (ConsumerDispatcherConfiguration<?, ?> config : getConfig().getConsumers()) {
			String logMessage = "Creating and starting consumer '" + config.getName() + "'.";
			withBeginEndLogging(KafkaConsumerService.class, logMessage, () -> createAndStartConsumer(context, config));
		}
	}

	/**
	 * Creates a {@link ConsumerDispatcher} from the given {@link ConsumerDispatcherConfiguration}
	 * and starts it.
	 */
	protected void createAndStartConsumer(InstantiationContext context, ConsumerDispatcherConfiguration<?, ?> config) {
		if (!TopicChecker.checkTopicsExists(context, config)) {
			/* No need to log it, as 'checkTopicExists' already logged an error. */
			return;
		}
		ConsumerDispatcher<?, ?> consumer = createConsumer(context, config);
		startConsumer(consumer);
	}

	/**
	 * Creates a {@link ConsumerDispatcher} from the given {@link ConsumerDispatcherConfiguration}.
	 */
	protected ConsumerDispatcher<?, ?> createConsumer(InstantiationContext context,
			ConsumerDispatcherConfiguration<?, ?> config) {
		ConsumerDispatcher<?, ?> consumer = context.getInstance(config);
		try {
			context.checkErrors();
		} catch (ConfigurationException ex) {
			// Module is not started completely. Shutdown already started consumer.
			logError("Startup failed. Shutting down the consumers which were already started.", ex);
			shutdownConsumer();
			throw new ConfigurationError(ex);
		}
		return consumer;
	}

	/** Starts the given {@link ConsumerDispatcher}. */
	protected void startConsumer(ConsumerDispatcher<?, ?> consumer) {
		ConsumerState consumerState = consumer.getConsumerState();
		synchronized (consumerState.getMutex()) {
			// start the thread first
			consumer.start();
			// register consumer as soon as it is started
			_consumers.add(consumer);

			int state = consumerState.awaitState(ConsumerState.POLLING, getConfig().getJoinTimeout());
			if (state != ConsumerState.POLLING) {
				logErrorConsumerNotPolling(consumer);
			}
		}
	}

	private void logErrorConsumerNotPolling(ConsumerDispatcher<?, ?> consumer) {
		StringBuilder errorStarting = new StringBuilder();
		errorStarting.append("Consumer '");
		errorStarting.append(consumer.getName());
		errorStarting.append("' does not start within ");
		errorStarting.append(getConfig().getJoinTimeout());
		errorStarting.append(" ms. Maybe Kafka not available?");
		logError(errorStarting.toString());
	}

	@Override
	public void shutDown() {
		withBeginEndLogging(KafkaConsumerService.class, "Shutting down and removing consumers.", this::shutdownConsumer);

		// call super-implementation last
		super.shutDown();
	}

	private void shutdownConsumer() {
		// terminate all consumers and remove terminated ones from service
		final Iterator<ConsumerDispatcher<?, ?>> iterator = _consumers.iterator();
		while (iterator.hasNext()) {
			ConsumerDispatcher<?, ?> consumer = iterator.next();
			String logMessage = "Shutting down and removing consumer '" + consumer.getName() + "'.";
			withBeginEndLogging(KafkaConsumerService.class, logMessage, () -> shutDownNextConsumer(consumer));
			iterator.remove();
		}
	}

	/**
	 * Shut down the next {@link ConsumerDispatcher} from the given {@link Iterator}.
	 * <p>
	 * The iterator has to have a next element.
	 * </p>
	 */
	protected void shutDownNextConsumer(ConsumerDispatcher<?, ?> consumer) {
		// request termination
		consumer.terminate();

		// await consumer termination
		awaitTermination(consumer);
	}

	/**
	 * a (possibly empty) {@link List} of all registered {@link ConsumerDispatcher}s
	 */
    public List<ConsumerDispatcher<?, ?>> getConsumers() {
        return (_consumers != null) ? Collections.unmodifiableList(_consumers) : Collections.<ConsumerDispatcher<?, ?>>emptyList();
    }

	/**
	 * @param name
	 *        the name of the {@link ConsumerDispatcher} to lookup
	 * @return the {@link ConsumerDispatcher} registered with this service under the given name or
	 *         {@code null} if none was found
	 */
	public ConsumerDispatcher<?, ?> getConsumer(final String name) {
        for (ConsumerDispatcher<?, ?> theConsumer : this.getConsumers()) {
			if (name.equals(theConsumer.getName())) {
                return theConsumer;
            }
        }

        return null;
    }

	/**
	 * Wait for the given thread to terminate.
	 * 
	 * @param thread
	 *            the {@link Thread} to await termination for
	 */
	protected void awaitTermination(Thread thread) {
		long stopTime = System.currentTimeMillis() + getConfig().getJoinTimeout();
		while (true) {
			if (!thread.isAlive()) {
				// terminated
				break;
			}
			long waitTime = stopTime - System.currentTimeMillis();
			if (waitTime <= 0) {
				StringBuilder errorStopping = new StringBuilder();
				errorStopping.append("Termination of '");
				errorStopping.append(thread.getName());
				errorStopping.append("' does not finished within ");
				errorStopping.append(getConfig().getJoinTimeout());
				errorStopping.append(" ms.");
				logError(errorStopping.toString());
				break;
			}
			try {
				thread.join(waitTime);
			} catch (InterruptedException e) {
				// ignore interruption when awaiting termination
			}
		}
	}

	private static void logError(String message) {
		Logger.error(message, KafkaConsumerService.class);
	}

	private static void logError(String message, Throwable throwable) {
		Logger.error(message, throwable, KafkaConsumerService.class);
	}

	/**
	 * the singleton {@link KafkaConsumerService} instance
	 */
	public static KafkaConsumerService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Module for {@link KafkaConsumerService}.
	 * 
	 * @author <a href="mailto:wta@top-logic.com">wta</a>
	 */
	public static final class Module extends TypedRuntimeModule<KafkaConsumerService> {

		/**
		 * Singleton {@link KafkaConsumerService.Module} instance.
		 */
		public static final KafkaConsumerService.Module INSTANCE = new KafkaConsumerService.Module();

		/**
		 * Creates a {@link Module}.
		 */
		private Module() {
			// enforce singleton pattern
		}

		@Override
		public Class<KafkaConsumerService> getImplementation() {
			return KafkaConsumerService.class;
		}
	}
}
