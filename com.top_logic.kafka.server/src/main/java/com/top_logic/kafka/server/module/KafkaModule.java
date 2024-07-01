/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.server.module;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.kafka.server.starter.KafkaStarter;

import kafka.Kafka;
import kafka.server.KafkaConfig;

/**
 * Module starting {@link Kafka}.
 * 
 * <p>
 * Actually a wrapper for {@link KafkaStarter}.
 * </p>
 * 
 * @see KafkaStarter
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@ServiceDependencies(ZooKeeperModule.Module.class)
public class KafkaModule extends ManagedClass {

	/**
	 * Configuration of the {@link KafkaModule}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ManagedClass.ServiceConfiguration<KafkaModule>, KafkaStarter.Config {

		// sum interface
	}

	private final KafkaStarter _kafkaStarter;

	/**
	 * Creates a new {@link KafkaModule} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link KafkaModule}.
	 */
	public KafkaModule(InstantiationContext context, Config config) {
		_kafkaStarter = new KafkaStarter(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();
		_kafkaStarter.startup();
	}

	@Override
	protected void shutDown() {
		_kafkaStarter.shutdown();
		super.shutDown();
	}

	/**
	 * Returns the configuration of the started {@link Kafka}.
	 */
	public KafkaConfig getKafkaConfig() {
		return _kafkaStarter.getKafkaConfig();
	}

	/**
	 * Module for the {@link KafkaModule}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class Module extends TypedRuntimeModule<KafkaModule> {

		/** Sole instance of {@link Module}. */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<KafkaModule> getImplementation() {
			return KafkaModule.class;
		}

	}

}

