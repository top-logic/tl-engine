/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;

/**
 * Base class for {@link TableConfigurationProvider}s that allow dynamically creating columns with
 * common configured properties.
 * 
 * @see #adaptDefaultConfiguration(ColumnConfiguration)
 * @see DynamicColumns.Config#getColumnDefault()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class DynamicColumns extends NoDefaultColumnAdaption
		implements ConfiguredInstance<DynamicColumns.Config> {

	private Config _config;

	/**
	 * Configuration options for {@link DynamicColumns}.
	 */
	public interface Config extends PolymorphicConfiguration<TableConfigurationProvider> {

		/**
		 * Configuration to apply to all dynamically added columns.
		 */
		@Name(TableConfig.COLUMN_DEFAULT_ATTRIBUTE)
		@ItemDefault
		ColumnConfig getColumnDefault();

	}

	private final ColumnConfigurator _columnConfigurator;

	/**
	 * Creates a {@link DynamicColumns} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DynamicColumns(InstantiationContext context, Config config) {
		_config = config;
		_columnConfigurator = TableConfigUtil.columnConfigurator(context, _config.getColumnDefault());
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	/**
	 * Installs the {@link Config#getColumnDefault()} configuration to the given column.
	 */
	protected final void adaptDefaultConfiguration(ColumnConfiguration column) {
		_columnConfigurator.adapt(column);
	}

}
