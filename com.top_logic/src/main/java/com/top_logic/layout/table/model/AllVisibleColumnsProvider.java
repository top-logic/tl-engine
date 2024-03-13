/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;
import com.top_logic.layout.table.provider.ColumnOptionLabelProvider;
import com.top_logic.layout.table.provider.ColumnOptionMapping;

/**
 * {@link TableConfigurationProvider} for selecting columns that can be chosen by a user.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@InApp
public class AllVisibleColumnsProvider extends NoDefaultColumnAdaption
		implements ConfiguredInstance<AllVisibleColumnsProvider.Config<?>> {

	/**
	 * Configuration of the {@link AllVisibleColumnsProvider}.
	 *
	 * @author <a href=mailto:sfo@top-logic.com>sfo</a>
	 */
	@DisplayOrder({
		Config.COLUMNS,
		Config.EXCLUDED,
	})
	public interface Config<I extends AllVisibleColumnsProvider> extends PolymorphicConfiguration<I> {

		/** Configuration name for {@link #getColumns()}. */
		String COLUMNS = "columns";

		/** Configuration name for {@link #isExcluded()}. */
		String EXCLUDED = "excluded";

		/**
		 * Column names for the configured types in its form model.
		 * 
		 * @see AllColumnsForConfiguredTypes
		 */
		@Options(fun = AllColumnsForConfiguredTypes.class, mapping = ColumnOptionMapping.class)
		@OptionLabels(ColumnOptionLabelProvider.class)
		@Format(CommaSeparatedStrings.class)
		@Name(COLUMNS)
		List<String> getColumns();

		/**
		 * Setter for {@link #getColumns()}.
		 */
		void setColumns(List<String> columns);

		/**
		 * Whether the selected {@link #getColumns()} must be excluded from the selectable columns.
		 * All other columns have their default visibility.
		 */
		@Name(EXCLUDED)
		boolean isExcluded();

		/**
		 * Setter for {@link #isExcluded()}.
		 */
		void setExcluded(boolean value);

	}

	private Config<?> _config;

	/**
	 * Creates a {@link AllVisibleColumnsProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AllVisibleColumnsProvider(InstantiationContext context, Config<?> config) {
		_config = config;
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		Set<String> columns = getColumns();

		for (ColumnConfiguration elementaryColumn : table.columns().getElementaryColumns()) {
			String name = elementaryColumn.getName();

			if (TableControl.SELECT_COLUMN_NAME.equals(name)) {
				continue;
			}
			if (getConfig().isExcluded()) {
				if (columns.contains(name)) {
					elementaryColumn.setVisibility(DisplayMode.excluded);
				}
			} else {
				if (columns.contains(name)) {
					if (elementaryColumn.getVisibility() == DisplayMode.excluded) {
						elementaryColumn.setVisibility(DisplayMode.hidden);
					}
				} else {
					elementaryColumn.setVisibility(DisplayMode.excluded);
				}
			}

		}

	}

	private Set<String> getColumns() {
		return new HashSet<>(getConfig().getColumns());
	}

	@Override
	public Config<?> getConfig() {
		return _config;
	}

}
