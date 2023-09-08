/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.knowledge.service;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.kafka.knowledge.service.exporter.KafkaExportConfiguration;
import com.top_logic.kafka.knowledge.service.exporter.ModelBasedExportConfiguration;
import com.top_logic.kafka.knowledge.service.importer.KafkaImportConfiguration;
import com.top_logic.kafka.knowledge.service.importer.ModelBasedImportConfiguration;
import com.top_logic.knowledge.service.PersistencyLayer;

/**
 * Configuration of the {@link KafkaExportConfiguration} and {@link KafkaImportConfiguration}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@ServiceDependencies(PersistencyLayer.Module.class)
public class KafkaExportImportConfiguration extends ManagedClass {

	/**
	 * Configuration of the {@link KafkaExportImportConfiguration}
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ServiceConfiguration<KafkaExportImportConfiguration> {

		/**
		 * Import configuration.
		 */
		@ItemDefault(ModelBasedImportConfiguration.class)
		PolymorphicConfiguration<KafkaImportConfiguration> getImport();

		/**
		 * Export configuration.
		 */
		@ItemDefault(ModelBasedExportConfiguration.class)
		PolymorphicConfiguration<KafkaExportConfiguration> getExport();

	}

	private final KafkaExportConfiguration _exportConfig;

	private final KafkaImportConfiguration _importConfig;

	/**
	 * Creates a new {@link KafkaExportImportConfiguration}.
	 */
	public KafkaExportImportConfiguration(InstantiationContext context, Config config) {
		super(context, config);
		_importConfig = context.getInstance(config.getImport());
		_exportConfig = context.getInstance(config.getExport());
	}

	/**
	 * Configured {@link KafkaExportConfiguration}.
	 */
	public static KafkaExportConfiguration getExportConfig() {
		return Module.INSTANCE.getImplementationInstance()._exportConfig;
	}

	/**
	 * Configured {@link KafkaImportConfiguration}.
	 */
	public static KafkaImportConfiguration getImportConfig() {
		return Module.INSTANCE.getImplementationInstance()._importConfig;
	}

	/**
	 * Module for the {@link KafkaExportImportConfiguration}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class Module extends TypedRuntimeModule<KafkaExportImportConfiguration> {

		/** Sole instance of {@link Module}. */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<KafkaExportImportConfiguration> getImplementation() {
			return KafkaExportImportConfiguration.class;
		}

	}

}

