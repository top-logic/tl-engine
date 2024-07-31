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
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfigurator;

/**
 * Configures the visibility of the column label in the header.
 */
@InApp
public class ColumnOptions extends AbstractConfiguredInstance<ColumnOptions.Config<?>>
		implements ColumnConfigurator {

	/**
	 * Configuration options for {@link ColumnOptions}.
	 */
	public interface Config<I extends ColumnOptions> extends PolymorphicConfiguration<I> {
		/**
		 * Whether the column label is displayed in the header.
		 */
		@BooleanDefault(true)
		boolean getShowHeader();

		/**
		 * Whether the column can be sorted.
		 */
		@BooleanDefault(true)
		boolean canSort();

		/**
		 * Whether the column can be filtered.
		 */
		@BooleanDefault(true)
		boolean canFilter();

		/**
		 * Whether a filter for this column is shown in a side-bar filter.
		 */
		@BooleanDefault(true)
		boolean showInSidebarFilter();

		/**
		 * Whether a row can be selected by clicking in a cell of this column.
		 */
		@BooleanDefault(true)
		boolean canSelect();
	}

	/**
	 * Creates a {@link ColumnOptions} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ColumnOptions(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void adapt(ColumnConfiguration column) {
		Config<?> config = getConfig();
		column.setSortable(config.canSort());
		if (!config.canFilter()) {
			column.setFilterProvider(null);
		}
		column.setExcludeFilterFromSidebar(!config.showInSidebarFilter());
		column.setShowHeader(config.getShowHeader());
		column.setSelectable(config.canSelect());
	}

}
