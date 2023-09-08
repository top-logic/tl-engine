/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.services.producer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.col.factory.CollectionFactory;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * A {@link ManagedClass} providing kafka producer services.
 * 
 * @author <a href="mailto:tgi@top-logic.com">tgi</a>
 */
public class KafkaProducerService extends ConfiguredManagedClass<KafkaProducerService.Config> {

	/**
	 * Typed configuration interface for {@link KafkaProducerService}.
	 * 
	 * @author <a href=mailto:tgi@top-logic.com>tgi</a>
	 */
	public interface Config extends ConfiguredManagedClass.Config<KafkaProducerService> {

		/**
		 * configured {@link TLKafkaProducer}'s by name
		 */
		@Key(TLKafkaProducer.Config.NAME_ATTRIBUTE)
		Map<String, ? extends TLKafkaProducer.Config<?, ?>> getProducers();

    }

	private Map<String, TLKafkaProducer<?, ?>> _producers;

	/**
	 * Create a {@link KafkaProducerService}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public KafkaProducerService(InstantiationContext context, Config config) {
		super(context, config);
		_producers = getInstanceMap(context, config);
    }

	/**
	 * Do not use factory method
	 * {@link TypedConfiguration#getInstanceMap(InstantiationContext, Map)}, because if an exception
	 * occurred, there may be TLKafkaProducer which are already created but never closed.
	 */
	private HashMap<String, TLKafkaProducer<?, ?>> getInstanceMap(InstantiationContext context, Config config) {
		HashMap<String, TLKafkaProducer<?, ?>> producers = CollectionFactory.map();
		for (Entry<String, ? extends TLKafkaProducer.Config<?, ?>> entry : config.getProducers().entrySet()) {
			TLKafkaProducer<?, ?> producer;
			try {
				producer = context.getInstance(entry.getValue());
			} catch (Exception ex) {
				context.error("Unable to create producer with name: " + entry.getKey(), ex);
				continue;
			}
			if (producer != null) {
				producers.put(entry.getKey(), producer);
			}
		}
		return producers;
	}

	/**
	 * @param producerName
	 *        the name of the producer to lookup
	 * @return the {@link StringTLKafkaProducer} registered with this service under the given name
	 *         or {@code null} if none was found
	 */
	public final TLKafkaProducer<?, ?> getProducer(final String producerName) {
		return _producers.get(producerName);
    }

	@Override
	protected void shutDown() {
		shutdownProducer();
		super.shutDown();
	}

	private void shutdownProducer() {
		// terminate all producers and remove terminated ones from service
		final Iterator<? extends TLKafkaProducer<?, ?>> iterator = _producers.values().iterator();
		while (iterator.hasNext()) {
			final TLKafkaProducer<?, ?> producer = iterator.next();

			// request termination
			producer.close();

			// remove terminated producer
			iterator.remove();
		}
	}

    /**
	 * the singleton {@link KafkaProducerService} instance
	 */
    public static KafkaProducerService getInstance() {
        return Module.INSTANCE.getImplementationInstance();
    }

	/**
	 * Module for {@link KafkaProducerService}.
	 * 
	 * @author <a href="mailto:tgi@top-logic.com">tgi</a>
	 */
	public static final class Module extends TypedRuntimeModule<KafkaProducerService> {

		/**
		 * Singleton {@link Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		/**
		 * Creates a {@link Module}.
		 */
		private Module() {
			// enforce singleton pattern
		}

		@Override
		public Class<KafkaProducerService> getImplementation() {
			return KafkaProducerService.class;
		}
	}
}
