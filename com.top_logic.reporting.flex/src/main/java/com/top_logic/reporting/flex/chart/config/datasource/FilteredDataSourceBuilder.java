/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.datasource;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.reporting.flex.chart.config.gui.ChartContextObserver;
import com.top_logic.reporting.flex.chart.config.gui.InteractiveBuilder;
import com.top_logic.reporting.flex.chart.config.gui.InteractiveBuilderUtil;

/**
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class FilteredDataSourceBuilder implements InteractiveBuilder<FilteredDataSource, ChartContextObserver>,
		ConfiguredInstance<FilteredDataSourceBuilder.Config> {

	/**
	 * Config-interface for {@link FilteredDataSourceBuilder}.
	 */
	public interface Config extends PolymorphicConfiguration<FilteredDataSourceBuilder> {
		
		/**
		 * <code>FILTERS</code> Attribute name for filters-property
		 */
		String FILTERS = "filters";

		/**
		 * <code>DATA_SOURCE</code> Attribute name for data-source-property
		 */
		String DATA_SOURCE = "data-source";

		/**
		 * the configured filters
		 */
		@InstanceFormat
		@Name(FILTERS)
		List<Filter<Object>> getFilters();
		
		/**
		 * the configured Builder for the providing a {@link ChartDataSource}
		 */
		@InstanceFormat
		@Name(DATA_SOURCE)
		InteractiveBuilder<ChartDataSource<DataContext>, ChartContextObserver> getDataSource();
		
	}

	private final Config _config;

	/**
	 * Config-Constructor for {@link FilteredDataSourceBuilder}. Uses the configured inner
	 * {@link ChartDataSource} and applies configured filters to create raw-data.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public FilteredDataSourceBuilder(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public void createUI(FormContainer container, ChartContextObserver observer) {

		InteractiveBuilderUtil.fillContainer(container, getConfig(), observer);

		template(container, div(
			member(Config.DATA_SOURCE),
			member(Config.FILTERS)));
	}

	@SuppressWarnings("unchecked")
	@Override
	public FilteredDataSource build(FormContainer container) {
		Map<String, Object> create = InteractiveBuilderUtil.create(container, getConfig());
		ChartDataSource<DataContext> dataSource = (ChartDataSource<DataContext>) create.get(Config.DATA_SOURCE);
		List<Filter<Object>> filters = (List<Filter<Object>>) create.get(Config.FILTERS);
		return FilteredDataSource.instance(dataSource, filters);
	}

}
