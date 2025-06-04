/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;

/**
 * {@link TableConfigurationProvider} for configuring the visibility of the select column.
 */
@InApp
@Label("Select column visibility")
public class SelectColumnVisibilityProvider extends AbstractConfiguredInstance<SelectColumnVisibilityProvider.Config<?>>
		implements TableConfigurationProvider {

	/**
	 * Configuration of {@link SelectColumnVisibilityProvider}.
	 */
	public interface Config<I extends SelectColumnVisibilityProvider> extends PolymorphicConfiguration<I> {

		/**
		 * Custom display mode of the select column.
		 */
		DisplayMode getVisibility();

	}

	/**
	 * Creates a {@link SelectColumnVisibilityProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public SelectColumnVisibilityProvider(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		ColumnConfiguration column = table.getDeclaredColumn(TableControl.SELECT_COLUMN_NAME);
		if (column != null) {
			column.setVisibility(getConfig().getVisibility());
		}
	}

}
