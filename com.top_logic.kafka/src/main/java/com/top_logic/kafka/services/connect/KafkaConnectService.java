/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.services.connect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * A {@link ManagedClass} implementing the kafka connect functionality using
 * {@link KafkaProducer} and {@link KafkaConsumer}.
 * 
 * @author <a href=mailto:wta@top-logic.com>wta</a>
 */
public class KafkaConnectService extends ConfiguredManagedClass<KafkaConnectService.Config> {

	/**
	 * Service interface configuration for {@link KafkaConnectService}.
	 * 
	 * @author <a href="mailto:wta@top-logic.com">wta</a>
	 */
	public interface Config extends ConfiguredManagedClass.Config<KafkaConnectService> {
		
		/**
		 * a (possibly empty) {@link List} of registered
		 *         {@link ConnectThread}s
		 */
		List<ConnectThread.Config> getConnectors();
		
		/**
		 * the amount of time (in milliseconds) to wait for a dispatcher
		 *         thread to terminate
		 */
		@LongDefault(1000)
		long getJoinTimeout();
	}

	/**
	 * A {@link List} of registered {@link ConnectThread}s.
	 */
	private final List<ConnectThread> _threads = new ArrayList<>();
	
	/**
	 * Create a {@link KafkaConnectService}.
	 * 
	 * @param context
	 *            the {@link InstantiationContext} to create the new object in
	 * @param config
	 *            the configuration object to be used for instantiation
	 */
	public KafkaConnectService(final InstantiationContext context, final Config config) {
		super(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();
		
		// startup all threads
		final InstantiationContext context = new DefaultInstantiationContext(KafkaConnectService.class);
		for (final PolymorphicConfiguration<ConnectThread> config : getConfig().getConnectors()) {
			final ConnectThread thread = context.getInstance(config);
			
			// start the thread first
			thread.start();
			
			// register the started thread
			_threads.add(thread);
		}
	}
	
	@Override
	protected void shutDown() {
		// terminate all threads and remove terminated ones from service
		final Iterator<ConnectThread> iterator = _threads.iterator();
		while(iterator.hasNext()) {
			final ConnectThread thread = iterator.next();

			// request termination
			thread.terminate();

			// await thread termination
			awaitTermination(thread);

			// remove terminated thread
			iterator.remove();
		}
		
		super.shutDown();
	}
	
	/**
	 * Wait for the given thread to terminate.
	 * 
	 * @param thread
	 *            the {@link Thread} to await termination for
	 */
	protected void awaitTermination(final Thread thread) {
		while (thread.isAlive()) {
			try {
				thread.join(getConfig().getJoinTimeout());
			} catch (InterruptedException e) {
				// ignore interruption when awaiting termination
			}
		}
	}
	
	/**
	 * @param config
	 *            the {@link ConfigurationItem} to resolve the kafka
	 *            configuration properties from
	 * @return the {@link Map} of key-value pairs
	 */
	static Map<String, Object> properties(final ConfigurationItem config) {
		final HashMap<String, Object> properties = new HashMap<>();
		
		for (final PropertyDescriptor property : config.descriptor().getProperties()) {
			final ConnectProperty annotation = property.getAnnotation(ConnectProperty.class);
			
			if(annotation != null) {
				final Object value = config.value(property);
				
				if(value != null) {
					properties.put(annotation.value(), value);
				}
			}
		}
		
		return properties;
	}
	
	/**
	 * the singleton {@link KafkaConnectService} instance
	 */
	public static KafkaConnectService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Module for {@link KafkaConnectService}.
	 * 
	 * @author <a href="mailto:wta@top-logic.com">wta</a>
	 */
	public static final class Module extends TypedRuntimeModule<KafkaConnectService> {

		/**
		 * Singleton {@link KafkaConnectService.Module} instance.
		 */
		public static final KafkaConnectService.Module INSTANCE = new KafkaConnectService.Module();

		/**
		 * Creates a {@link Module}.
		 */
		private Module() {
			// enforce singleton pattern
		}

		@Override
		public Class<KafkaConnectService> getImplementation() {
			return KafkaConnectService.class;
		}
	}
}
