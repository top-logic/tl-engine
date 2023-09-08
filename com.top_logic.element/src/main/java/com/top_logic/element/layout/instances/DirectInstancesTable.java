/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.instances;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.knowledge.wrap.WrapperAccessor;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.provider.GenericTableConfigurationProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.annotate.DisplayAnnotations;

/**
 * {@link TableConfigurationProvider} that configures a table with it's surrounding component's
 * model (expecting it to be a {@link TLClass}).
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DirectInstancesTable<C extends DirectInstancesTable.Config<?>> extends AbstractConfiguredInstance<C>
		implements TableConfigurationProvider {

	/** Name of the column displaying the default view for the row object. */
	private static final String DEFAULT_VIEW_COLUMN_NAME = "__default_view";

	/**
	 * Configuration options for {@link DirectInstancesTable}.
	 */
	public interface Config<I extends DirectInstancesTable<?>> extends PolymorphicConfiguration<I> {
		// Pure marker.
	}

	private LayoutComponent _component;

	/**
	 * Creates a {@link DirectInstancesTable} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DirectInstancesTable(InstantiationContext context, C config) {
		super(context, config);

		context.resolveReference(InstantiationContext.OUTER, LayoutComponent.class, c -> _component = c);
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		Object model = _component.getModel();
		if (!(model instanceof TLClass)) {
			return;
		}
		TLClass type = (TLClass) model;
		GenericTableConfigurationProvider.getTableConfigurationProvider(type).adaptConfigurationTo(table);

		List<String> allProperties = new ArrayList<>(DisplayAnnotations.getMainProperties(type));

		// Enforce displaying all columns to avoid creating instances with mandatory fields not
		// being set.
		table.getDeclaredColumns().stream().forEach(c -> c.setVisibility(DisplayMode.mandatory));

		allProperties.addAll(table.getDeclaredColumns().stream().map(ColumnConfiguration::getName)
			.filter(name -> !allProperties.contains(name) && !name.equals(TableControl.SELECT_COLUMN_NAME))
			.collect(Collectors.toList()));

		table.setDefaultColumns(allProperties);
		addDefaultViewColumn(table);
	}

	private void addDefaultViewColumn(TableConfiguration table) {
		ColumnConfiguration viewColumn = table.declareColumn(DEFAULT_VIEW_COLUMN_NAME);
		viewColumn.setAccessor(new ReadOnlyAccessor<Object>() {

			@Override
			public Object getValue(Object object, String property) {
				return object;
			}
		});
		viewColumn.setVisibility(DisplayMode.hidden);
		viewColumn.setColumnLabelKey(I18NConstants.DEFAULT_VIEW_COLUMN_LABEL);
	}

	@Override
	public void adaptDefaultColumn(ColumnConfiguration defaultColumn) {
		defaultColumn.setAccessor(WrapperAccessor.INSTANCE);
	}

}
