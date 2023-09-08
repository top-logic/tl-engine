/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.annotate.ui.TLIDColumn;
import com.top_logic.model.annotate.ui.TLSortColumns;

/**
 * {@link TableConfigurationProvider} to set the tables sort columns order by which the rows are
 * sorted.
 * 
 * <p>
 * If the table has no sort columns order set then the table rows are sorted after the id column if
 * it exists.
 * </p>
 * 
 * @see TLIDColumn
 * @see IDColumnTableConfigurationProvider
 * @see SortColumnsConfig
 * @see TLSortColumns
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@InApp
public class SortColumnsTableConfigurationProvider extends NoDefaultColumnAdaption
		implements ConfiguredInstance<SortColumnsTableConfigurationProvider.Config> {

	/**
	 * Configuration of the {@link SortColumnsTableConfigurationProvider}.
	 *
	 * @author <a href=mailto:sfo@top-logic.com>sfo</a>
	 */
	@TagName(SortColumnsConfig.SORT_COLUMNS)
	public interface Config extends PolymorphicConfiguration<SortColumnsTableConfigurationProvider>, SortColumnsConfig {

		// Marker interface.

	}

	private Config _config;

	/**
	 * Creates a {@link SortColumnsTableConfigurationProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public SortColumnsTableConfigurationProvider(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		table.setDefaultSortOrder(_config.getOrder());
	}

	@Override
	public Config getConfig() {
		return _config;
	}

}
