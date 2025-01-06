/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.Step;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.func.Function4;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.editor.config.TypesTemplateParameters;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.table.provider.AllColumnOptions;
import com.top_logic.layout.table.provider.ColumnOption;
import com.top_logic.layout.table.provider.ColumnOptionLabelProvider;
import com.top_logic.layout.table.provider.ColumnOptionMapping;
import com.top_logic.layout.table.provider.ColumnProviderConfig;
import com.top_logic.model.util.TLModelPartRef;

/**
 * {@link TableConfigurationProvider} to create a column group.
 */
@InApp
@Label("Column group")
public class ColumnGroupTableConfigurationProvider
		extends AbstractConfiguredInstance<ColumnGroupTableConfigurationProvider.Config>
		implements TableConfigurationProvider {

	/**
	 * Typed configuration interface definition for {@link ColumnGroupTableConfigurationProvider}.
	 */
	public interface Config
			extends PolymorphicConfiguration<ColumnGroupTableConfigurationProvider>, ConfigPart, ColumnProviderConfig {

		/**
		 * @see #getContext()
		 */
		String CONTEXT = "context";

		/**
		 * The columns that are displayed in the column group.
		 */
		@Mandatory
		@Options(fun = AllColumnOptionsButMe.class, mapping = ColumnOptionMapping.class, args = {
			@Ref(steps = {
				@Step(Config.CONTEXT),
				@Step(type = TypesTemplateParameters.class, value = TypesTemplateParameters.TYPE) }),
			@Ref(steps = {
				@Step(Config.CONTEXT),
				@Step(type = WithConfigurationProviders.class, value = WithConfigurationProviders.CONFIGURATION_PROVIDERS) }),
			@Ref(steps = {
				@Step(Config.CONTEXT),
				@Step(type = WithConfigurationProviders.class, value = WithConfigurationProviders.CONFIGURATION_PROVIDERS),
				@Step(type = ColumnProviderConfig.class, value = ColumnProviderConfig.COLUMN_LABEL) }),
			@Ref(steps = @Step(COLUMN_ID))
		})
		@OptionLabels(value = ColumnOptionLabelProvider.class)
		@Format(CommaSeparatedStrings.class)
		List<String> getInnerColumns();

		/**
		 * {@link AllColumnOptions} without this column.
		 */
		class AllColumnOptionsButMe extends
				Function4<Collection<ColumnOption>, Collection<TLModelPartRef>, Collection<PolymorphicConfiguration<? extends TableConfigurationProvider>>, Object, String> {

			@Override
			public Collection<ColumnOption> apply(Collection<TLModelPartRef> arg1,
					Collection<PolymorphicConfiguration<? extends TableConfigurationProvider>> arg2, Object arg3,
					String arg4) {
				Collection<ColumnOption> columnOptions = AllColumnOptions.INSTANCE.apply(arg1, arg2, arg3);
				return columnOptions.stream()
					.filter(Predicate.not(option -> option.getTechnicalName().equals(arg4)))
					.toList();
			}

		}

		/**
		 * The context in which this provider is configured.
		 * 
		 * <p>
		 * Used for looking up columns that are declared in this context.
		 * </p>
		 */
		@Name(CONTEXT)
		@Hidden
		@Container
		ConfigurationItem getContext();
	}

	/**
	 * Create a {@link ColumnGroupTableConfigurationProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ColumnGroupTableConfigurationProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		ColumnConfiguration columnGroup = table.declareColumn(getConfig().getColumnId());
		columnGroup.setColumnLabelKey(getConfig().getColumnLabel());
		for (String innerColumn : getConfig().getInnerColumns()) {
			ColumnConfiguration declaredColumn = table.getDeclaredColumn(innerColumn);
			if (declaredColumn == null) {
				InfoService.showWarning(I18NConstants.NO_SUCH_COLUMN__COLUMN.fill(declaredColumn));
				continue;
			}
			columnGroup.addColumn(declaredColumn);
		}

	}

}
