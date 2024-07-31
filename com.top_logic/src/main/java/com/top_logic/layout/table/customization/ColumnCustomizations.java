/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.customization;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.table.customization.ColumnCustomizations.Config.ColumnCustomization;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfigurator;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationProvider;

/**
 * {@link TableConfigurationProvider} that applies {@link ColumnConfigurator}s to certain columns.
 */
@InApp
public class ColumnCustomizations extends AbstractConfiguredInstance<ColumnCustomizations.Config<?>>
		implements TableConfigurationProvider {

	/**
	 * Configuration options for {@link ColumnCustomizations}.
	 */
	public interface Config<I extends ColumnCustomizations> extends PolymorphicConfiguration<I> {

		/**
		 * Bundle of {@link ColumnConfigurator}s for a certain column.
		 */
		@DisplayOrder({ ColumnCustomization.NAME, ColumnCustomization.CONFIGURATIONS })
		interface ColumnCustomization extends ConfigurationItem {

			/**
			 * @see #getName()
			 */
			String NAME = "name";

			/**
			 * @see #getConfigurators()
			 */
			String CONFIGURATIONS = "configurations";

			@Name(NAME)
			@Mandatory
			String getName();

			@Name(CONFIGURATIONS)
			@DefaultContainer
			@Options(fun = AllInAppImplementations.class)
			List<PolymorphicConfiguration<? extends ColumnConfigurator>> getConfigurators();
		}

		/**
		 * {@link ColumnCustomization}s for all columns that should receive special configuration.
		 */
		@DefaultContainer
		@Key(ColumnCustomization.NAME)
		List<ColumnCustomization> getColumns();
	}

	private final List<TableConfigurationProvider> _providers = new ArrayList<>();

	/**
	 * Creates a {@link ColumnCustomizations} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ColumnCustomizations(InstantiationContext context, Config<?> config) {
		super(context, config);

		for (ColumnCustomization column : config.getColumns()) {
			String columnName = column.getName();
			List<ColumnConfigurator> configurators = TypedConfiguration.getInstanceList(context, column.getConfigurators());
			
			_providers.add(table -> {
				ColumnConfiguration columnConfig = table.declareColumn(columnName);
				for (ColumnConfigurator configurator : configurators) {
					configurator.adapt(columnConfig);
				}
			});
		}
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		for (TableConfigurationProvider provider : _providers) {
			provider.adaptConfigurationTo(table);
		}
	}

}
