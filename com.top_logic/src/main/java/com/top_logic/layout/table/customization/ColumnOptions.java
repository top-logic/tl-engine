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
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfigurator;
import com.top_logic.layout.table.provider.generic.SuppressExportConfigurator;

/**
 * Configures several options for the column.
 * 
 * <p>
 * Besides others, it can be chosen, whether the column can be sorted or filtered.
 * </p>
 */
@InApp
public class ColumnOptions extends AbstractConfiguredInstance<ColumnOptions.Config<?>>
		implements ColumnConfigurator {

	/**
	 * Configuration options for {@link ColumnOptions}.
	 */
	@TagName("column-options")
	@DisplayOrder({
		Config.SELECT_NAME,
		Config.SORT_NAME,
		Config.FILTER_NAME,
		Config.IN_SIDEBAR_FILTER_NAME,
		Config.SHOW_HEADER_NAME,
		Config.EXPORT_NAME,
	})
	public interface Config<I extends ColumnOptions> extends PolymorphicConfiguration<I> {

		/** Configuration name for {@link #getShowHeader()}. */
		String SHOW_HEADER_NAME = "show-header";

		/** Configuration name for {@link #canSort()}. */
		String SORT_NAME = "sort";

		/** Configuration name for {@link #canFilter()}. */
		String FILTER_NAME = "filter";

		/** Configuration name for {@link #showInSidebarFilter()}. */
		String IN_SIDEBAR_FILTER_NAME = "show-in-sidebar-filter";

		/** Configuration name for {@link #canSelect()}. */
		String SELECT_NAME = "select";

		/** Configuration name for {@link #canExport()}. */
		String EXPORT_NAME = "export";

		/**
		 * Whether the column label is displayed in the header.
		 */
		@BooleanDefault(true)
		@Name(SHOW_HEADER_NAME)
		boolean getShowHeader();

		/**
		 * Whether the column can be sorted.
		 */
		@BooleanDefault(true)
		@Name(SORT_NAME)
		boolean canSort();

		/**
		 * Whether the column can be filtered.
		 */
		@BooleanDefault(true)
		@Name(FILTER_NAME)
		boolean canFilter();

		/**
		 * Whether a filter for this column is shown in a side-bar filter.
		 */
		@BooleanDefault(true)
		@Name(IN_SIDEBAR_FILTER_NAME)
		boolean showInSidebarFilter();

		/**
		 * Whether a row can be selected by clicking in a cell of this column.
		 */
		@BooleanDefault(true)
		@Name(SELECT_NAME)
		boolean canSelect();

		/**
		 * Whether the value for this column is exported to Excel.
		 */
		@BooleanDefault(true)
		@Name(EXPORT_NAME)
		boolean canExport();
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
		if (!config.canExport()) {
			SuppressExportConfigurator.INSTANCE.adapt(column);
		}
	}

}
