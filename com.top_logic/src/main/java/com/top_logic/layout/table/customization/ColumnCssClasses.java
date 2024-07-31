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
 * Configures CSS classes to add to a column's cells.
 */
@InApp
public class ColumnCssClasses extends AbstractConfiguredInstance<ColumnCssClasses.Config<?>>
		implements ColumnConfigurator {

	/**
	 * Configuration options for {@link ColumnCssClasses}.
	 */
	public interface Config<I extends ColumnCssClasses> extends PolymorphicConfiguration<I> {
		/**
		 * CSS class to apply to all cells in a column.
		 */
		String getCssClass();

		/**
		 * The CSS class to add to the column's header.
		 */
		String getCssHeaderClass();
	}

	/**
	 * Creates a {@link ColumnCssClasses} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ColumnCssClasses(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void adapt(ColumnConfiguration column) {
		column.setCssClass(getConfig().getCssClass());
		column.setCssHeaderClass(getConfig().getCssHeaderClass());
	}

}
