/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.CommaSeparatedStringSet;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
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
@Label("Available columns")
public class AllVisibleColumnsProvider extends AbstractConfiguredInstance<AllVisibleColumnsProvider.Config<?>>
		implements TableConfigurationProvider {

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
		 * Names of columns that can be choosen by the user (or cannot be choosen by the user
		 * depending on the {@link #isExcluded()} flag).
		 */
		@Options(fun = AllColumnsForConfiguredTypes.class, mapping = ColumnOptionMapping.class)
		@OptionLabels(ColumnOptionLabelProvider.class)
		@Format(CommaSeparatedStringSet.class)
		@Name(COLUMNS)
		Set<String> getColumns();

		/**
		 * Setter for {@link #getColumns()}.
		 */
		void setColumns(Set<String> columns);

		/**
		 * Whether the selected {@link #getColumns()} should be excluded from the selectable
		 * columns. All other columns have their default visibility.
		 */
		@Name(EXCLUDED)
		@Label("Exclude selected columns")
		boolean isExcluded();

		/**
		 * Setter for {@link #isExcluded()}.
		 */
		void setExcluded(boolean value);
	}

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
		super(context, config);
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		Set<String> configuredColumns = getConfig().getColumns();
		boolean excludeColumns = getConfig().isExcluded();

		for (ColumnConfiguration elementaryColumn : table.columns().getElementaryColumns()) {
			String columnName = elementaryColumn.getName();
			if (TableControl.SELECT_COLUMN_NAME.equals(columnName)) {
				// Handled above.
				continue;
			}

			if (excludeColumns) {
				if (configuredColumns.contains(columnName)) {
					elementaryColumn.setVisibility(DisplayMode.excluded);
				}
			} else {
				if (configuredColumns.contains(columnName)) {
					if (elementaryColumn.getVisibility() == DisplayMode.excluded) {
						elementaryColumn.setVisibility(DisplayMode.hidden);
					}
				} else {
					elementaryColumn.setVisibility(DisplayMode.excluded);
				}
			}
		}
	}

}
