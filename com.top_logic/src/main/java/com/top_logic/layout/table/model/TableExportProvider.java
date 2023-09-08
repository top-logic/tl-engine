/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;

/**
 * Configurable {@link TableConfigurationProvider} that adds an {@link TableDataExport} to a
 * {@link TableConfiguration}.
 */
@Label("Table export")
public class TableExportProvider extends NoDefaultColumnAdaption
		implements ConfiguredInstance<TableExportProvider.Config<?>> {

	private final Config<?> _config;

	private final TableDataExport _exporter;

	/**
	 * Configuration options for {@link TableExportProvider}.
	 */
	public interface Config<I extends TableExportProvider> extends PolymorphicConfiguration<I> {

		/**
		 * Export configuration that describes the table export to add.
		 */
		@NonNullable
		@ItemDefault(SimpleTableDataExport.class)
		PolymorphicConfiguration<? extends TableDataExport> getExport();

	}

	/**
	 * Creates a {@link TableExportProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TableExportProvider(InstantiationContext context, Config<?> config) {
		_config = config;
		_exporter = context.getInstance(config.getExport());
	}

	@Override
	public Config<?> getConfig() {
		return _config;
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		table.setExporter(_exporter);
	}

}
