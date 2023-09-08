/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.url;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.chart.urls.XYZURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.AbstractSeriesDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.xy.XYZDataset;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Filter;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartContext;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartData;
import com.top_logic.reporting.flex.chart.config.model.ChartNode;

/**
 * Default url-generator-provider that works with {@link CategoryPlot}, {@link XYPlot} and
 * {@link PiePlot}.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class DefaultURLGeneratorProvider implements XYURLGeneratorProvider, CategoryURLGeneratorProvider,
		PieURLGeneratorProvider, XYZURLGeneratorProvider {

	/**
	 * Default-filter for {@link ChartNode}s that accepts only nodes containing objects.
	 */
	static final Filter<ChartNode> DEFAULT_FILTER = new Filter<>() {

		@Override
		public boolean accept(ChartNode node) {
			return node != null && !CollectionUtil.isEmptyOrNull(node.getObjects());
		}
	};

	/**
	 * Singleton <code>INSTANCE</code> of {@link DefaultURLGeneratorProvider} that creates links for
	 * not-empty {@link ChartNode}s.
	 */
	public static final DefaultURLGeneratorProvider INSTANCE = new DefaultURLGeneratorProvider(DEFAULT_FILTER);

	private final Filter<ChartNode> _filter;

	/**
	 * Creates a new {@link DefaultURLGeneratorProvider} that creates links for ChartNodes
	 * containing objects.
	 */
	protected DefaultURLGeneratorProvider() {
		this(DEFAULT_FILTER);
	}

	/**
	 * Creates a new {@link DefaultURLGeneratorProvider} that creates links for ChartsNodes which
	 * are accepted by the given filter.
	 * 
	 * @param filter
	 *        the filter that decides if a link should be created for a ChartNode - must not be
	 *        null.
	 */
	protected DefaultURLGeneratorProvider(Filter<ChartNode> filter) {
		_filter = filter;
	}

	@Override
	public PieURLGenerator getPieTooltipGenerator(JFreeChart model, ChartContext context,
			ChartData<? extends PieDataset> chartData) {
		return new JFreeChartURLGenerator(context, chartData.getModel(), _filter);
	}

	@Override
	public CategoryURLGenerator getCategoryURLGenerator(JFreeChart model, ChartContext context,
			ChartData<? extends CategoryDataset> chartData) {
		return new JFreeChartURLGenerator(context, chartData.getModel(), _filter);
	}

	@Override
	public XYURLGenerator getXYURLGenerator(JFreeChart model, ChartContext context,
			ChartData<? extends AbstractSeriesDataset> chartData) {
		return new JFreeChartURLGenerator(context, chartData.getModel(), _filter);
	}

	@Override
	public XYZURLGenerator getXYZURLGenerator(JFreeChart model, ChartContext context,
			ChartData<? extends XYZDataset> chartData) {
		return new JFreeChartURLGenerator(context, chartData.getModel(), _filter);
	}

}
