/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.url;

import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.chart.urls.XYZURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.CategoryToPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;

import com.top_logic.basic.col.Filter;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartContext;
import com.top_logic.reporting.flex.chart.config.dataset.CategoryDatasetBuilder;
import com.top_logic.reporting.flex.chart.config.dataset.TimeseriesDatasetBuilder;
import com.top_logic.reporting.flex.chart.config.dataset.XYSeriesBuilder;
import com.top_logic.reporting.flex.chart.config.dataset.XYZDatasetBuilder;
import com.top_logic.reporting.flex.chart.config.model.ChartNode;
import com.top_logic.reporting.flex.chart.config.model.ChartTree;
import com.top_logic.reporting.flex.chart.config.model.ChartTree.DataKey;

/**
 * Default URLGenerator that can be used with XY-Charts, Category-Charts and Pie-Charts.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class JFreeChartURLGenerator implements XYURLGenerator, CategoryURLGenerator, PieURLGenerator, XYZURLGenerator {

	private final ChartContext _context;

	private final ChartTree _tree;

	private final Filter<ChartNode> _filter;

	/**
	 * Creates a new {@link JFreeChartURLGenerator}
	 */
	public JFreeChartURLGenerator(ChartContext context, ChartTree tree) {
		this(context, tree, DefaultURLGeneratorProvider.DEFAULT_FILTER);
	}

	/**
	 * Creates a new {@link JFreeChartURLGenerator}
	 */
	public JFreeChartURLGenerator(ChartContext context, ChartTree tree, Filter<ChartNode> filter) {
		_context = context;
		_tree = tree;
		_filter = filter;
	}

	/**
	 * the chart-context used to create URLs
	 */
	protected ChartContext getContext() {
		return _context;
	}

	/**
	 * the universal chart-model for context-information
	 */
	protected ChartTree getTree() {
		return _tree;
	}

	@Override
	public String generateURL(XYDataset dataset, int series, int item) {
		DataKey dataKey = null;
		if (dataset instanceof XYZDataset) {
			return generateURL((XYZDataset) dataset, series, item);
		}
		else if (dataset instanceof TimeSeriesCollection) {
			dataKey = TimeseriesDatasetBuilder.toDataKey((TimeSeriesCollection) dataset, series, item);
		}
		else {
			dataKey = XYSeriesBuilder.toDataKey(dataset, series, item);
		}
		return getUrl(dataKey);
	}

	@Override
	public String generateURL(XYZDataset dataset, int series, int item) {
		DataKey dataKey = XYZDatasetBuilder.toDataKey(dataset, series, item);
		return getUrl(dataKey);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public String generateURL(PieDataset dataset, Comparable key, int pieIndex) {
		int index = dataset.getIndex(key);
		CategoryDataset categoryDataset = ((CategoryToPieDataset) dataset).getUnderlyingDataset();
		Object dataKey = CategoryDatasetBuilder.toDataKey(categoryDataset, index, pieIndex);
		return getUrl(dataKey);
	}

	@Override
	public String generateURL(CategoryDataset dataset, int series, int category) {
		Object dataKey = CategoryDatasetBuilder.toDataKey(dataset, series, category);
		return getUrl(dataKey);
	}

	/**
	 * Hook for subclasses. The default-method creates an URL with the node-ID if it contains
	 * objects.
	 * 
	 * @return the URL to render in the chart
	 */
	protected String getUrl(Object dataKey) {
		ChartNode node = _tree.getNode((DataKey) dataKey);
		if (node == null || !_filter.accept(node)) {
			return null;
		}

		return _context.getUrl(node.getID());
	}
}
