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

/**
 * {@link TableConfigurationProvider} to set the id column of a table.
 * 
 * <p>
 * The id column is the column for which an icon of the row objects type is added before its value
 * and which is rendered as a link to its row object.
 * </p>
 * 
 * @see IDColumnConfig
 * @see TLIDColumn
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@InApp
public class IDColumnTableConfigurationProvider extends NoDefaultColumnAdaption
		implements ConfiguredInstance<IDColumnTableConfigurationProvider.Config> {

	/**
	 * Configuration of the {@link IDColumnTableConfigurationProvider}.
	 *
	 * @author <a href=mailto:sfo@top-logic.com>sfo</a>
	 */
	@TagName(IDColumnConfig.ID_COLUMN)
	public interface Config extends PolymorphicConfiguration<IDColumnTableConfigurationProvider>, IDColumnConfig {

		// Marker interface.

	}

	private Config _config;

	/**
	 * Creates a {@link IDColumnTableConfigurationProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public IDColumnTableConfigurationProvider(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		table.setIDColumn(_config.getValue());
	}

	@Override
	public Config getConfig() {
		return _config;
	}

}
