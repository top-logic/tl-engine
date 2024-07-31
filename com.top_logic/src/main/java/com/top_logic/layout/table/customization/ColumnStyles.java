/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.customization;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfigurator;

/**
 * Configures CSS styles to add to a column.
 * 
 * <p>
 * Note: Whenever possible it is better to use CSS classes instead of direct styling to allow style
 * adjustments in the user's theme.
 * </p>
 */
@InApp
public class ColumnStyles extends AbstractConfiguredInstance<ColumnStyles.Config<?>>
		implements ColumnConfigurator {

	/**
	 * Configuration options for {@link ColumnStyles}.
	 */
	public interface Config<I extends ColumnStyles> extends PolymorphicConfiguration<I> {

		/**
		 * CSS styles to add to the columns's header.
		 */
		String getHeadStyle();

		/**
		 * CSS styles to add to all cells of the column.
		 */
		String getCellStyle();

	}

	/**
	 * Creates a {@link ColumnStyles} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ColumnStyles(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void adapt(ColumnConfiguration column) {
		column.setHeadStyle(getConfig().getHeadStyle());
		column.setCellStyle(getConfig().getCellStyle());
	}

}
