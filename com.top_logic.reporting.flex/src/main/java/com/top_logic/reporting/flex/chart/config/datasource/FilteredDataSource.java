/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.datasource;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.element.meta.query.CollectionFilter;

/**
 * {@link ChartDataSource}-implementation that uses a nested data-source and configured filters.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class FilteredDataSource implements ChartDataSource<DataContext> {

	@SuppressWarnings("rawtypes")
	private static final Filter COLL_FILTER = new Filter<Filter<?>>() {

		@Override
		public boolean accept(Filter<?> anObject) {
			return anObject instanceof CollectionFilter;
		}
	};

	@SuppressWarnings("rawtypes")
	private static final Filter FILTER = new Filter<Filter<?>>() {

		@Override
		public boolean accept(Filter<?> anObject) {
			return !(anObject instanceof CollectionFilter);
		}
	};

	private final ChartDataSource<DataContext> _dataSource;
	private final List<Filter<Object>> _filters;

	private final List<CollectionFilter> _collectionFilters;

	/**
	 * Config-interface for {@link FilteredDataSource}.
	 */
	public interface Config extends PolymorphicConfiguration<FilteredDataSource> {

		/**
		 * the inner data-source that provides the objects to apply the filters to
		 */
		@InstanceFormat
		public ChartDataSource<DataContext> getDataSource();

		/**
		 * See {@link #getDataSource()}
		 */
		public void setDataSource(ChartDataSource<DataContext> datasource);

		/**
		 * the list of filters to apply to the objects provided by the inner data-source
		 */
		@InstanceFormat
		public List<Filter<Object>> getFilters();

		/**
		 * See {@link #getFilters()}
		 */
		public void setFilters(List<Filter<Object>> filters);
	}


	/**
	 * Config-constructor for {@link FilteredDataSource}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	@SuppressWarnings("unchecked")
	public FilteredDataSource(InstantiationContext context, Config config) {
		_dataSource = config.getDataSource();
		_collectionFilters = FilterUtil.filterList(COLL_FILTER, config.getFilters());
		_filters = FilterUtil.filterList(FILTER, config.getFilters());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<? extends Object> getRawData(DataContext context) {
		Collection<? extends Object> rawData = _dataSource.getRawData(context);
		for (CollectionFilter filter : _collectionFilters) {
			try {
				rawData = filter.filter(rawData);
			} catch (Exception ex) {
				throw new RuntimeException();
			}
			if (rawData.isEmpty()) {
				break;
			}
		}
		return FilterUtil.filterAnd(_filters, rawData);
	}

	/**
	 * Creates a new {@link Config} initialized with the given values for filters and datasource.
	 */
	public static Config item(ChartDataSource<DataContext> dataSource, List<Filter<Object>> filters) {
		Config item = TypedConfiguration.newConfigItem(Config.class);
		item.setFilters(filters);
		item.setDataSource(dataSource);
		return item;
	}

	/**
	 * Instanciates a new {@link FilteredDataSource} initialized with the given values for filters
	 * and datasource.
	 */
	public static FilteredDataSource instance(ChartDataSource<DataContext> dataSource, List<Filter<Object>> filters) {
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(item(dataSource, filters));
	}

}
