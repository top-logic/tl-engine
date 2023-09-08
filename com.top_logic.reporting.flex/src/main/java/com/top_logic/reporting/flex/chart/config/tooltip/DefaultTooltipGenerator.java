/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.tooltip;

import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.labels.XYZToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.CategoryToPieDataset;
import org.jfree.data.general.AbstractSeriesDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYZDataset;

import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartContext;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartData;
import com.top_logic.reporting.flex.chart.config.dataset.CategoryDatasetBuilder;
import com.top_logic.reporting.flex.chart.config.dataset.TimeseriesDatasetBuilder;
import com.top_logic.reporting.flex.chart.config.dataset.XYSeriesBuilder;
import com.top_logic.reporting.flex.chart.config.dataset.XYSeriesBuilder.XYSeriesDataKey;
import com.top_logic.reporting.flex.chart.config.dataset.XYZDatasetBuilder;
import com.top_logic.reporting.flex.chart.config.model.ChartNode;
import com.top_logic.reporting.flex.chart.config.model.ChartTree.DataKey;
import com.top_logic.reporting.flex.chart.config.partition.CoordinatePartition.Coordinate;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.CoordinateCriterion;

/**
 * Default tooltip-generator that works with {@link CategoryPlot}, {@link XYPlot} and
 * {@link PiePlot}.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class DefaultTooltipGenerator implements PieToolTipGenerator,
		CategoryToolTipGenerator, XYZToolTipGenerator {

	private final ChartData<? extends Dataset> _chartData;

	/**
	 * Creates a new {@link DefaultTooltipGenerator} for the given chart-data
	 * 
	 * @param chartData
	 *        the chart-data for context-information
	 */
	public DefaultTooltipGenerator(ChartData<? extends Dataset> chartData) {
		_chartData = chartData;
	}

	@Override
	public String generateToolTip(CategoryDataset dataset, int row, int column) {
		DataKey dataKey = CategoryDatasetBuilder.toDataKey(dataset, row, column);
		return generateTooltip(dataKey);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public String generateToolTip(PieDataset dataset, Comparable key) {
		if (dataset instanceof CategoryToPieDataset) {
			CategoryToPieDataset pieDataset = (CategoryToPieDataset) dataset;
			CategoryDataset categoryDataset = pieDataset.getUnderlyingDataset();
			int index = dataset.getIndex(key);
			return generateToolTip(categoryDataset, index, pieDataset.getExtractIndex());
		}
		return null;
	}

	@Override
	public String generateToolTip(XYDataset dataset, int series, int item) {
		if (dataset instanceof XYZDataset) {
			return generateToolTip((XYZDataset) dataset, series, item);
		}
		DataKey dataKey = null;
		if (dataset instanceof TimeSeriesCollection) {
			dataKey = TimeseriesDatasetBuilder.toDataKey((TimeSeriesCollection) dataset, series, item);
		}
		else if (dataset instanceof XYSeriesCollection) {
			dataKey = XYSeriesBuilder.toDataKey(dataset, series, item);
		}
		return generateTooltip(dataKey);
	}

	/**
	 * Hook for subclasses
	 */
	protected String generateTooltip(DataKey dataKey) {
		ChartNode node = _chartData.getModel().getNode(dataKey);
		if (node == null) {
			return null;
		}
		return generateTooltip(dataKey, node);
	}

	@Override
	public String generateToolTip(XYZDataset dataset, int series, int item) {
		XYSeriesDataKey dataKey = XYZDatasetBuilder.toDataKey(dataset, series, item);

		ChartNode node = _chartData.getModel().getNode(dataKey);
		int posZ = node.getTree().getCriterionIndex(CoordinateCriterion.class, Coordinate.Z);

		List<ChartNode> nodes = new ArrayList<>();
		nodes.add(node);
		ChartNode parent = node.getParent();
		while (parent != null) {
			nodes.add(0, parent);
			parent = parent.getParent();
		}
		if (nodes.size() > posZ + 1) {
			ChartNode chartNode = nodes.get(posZ + 1);
			return dataKey.getKey() + ": " + MetaLabelProvider.INSTANCE.getLabel(chartNode.getKey());
		}

		return "";
	}

	/**
	 * Creates a simple default-tooltip based on the data-key and the value of the node.
	 * Overwrite this for custom tooltips.
	 * 
	 * @param dataKey
	 *        the datakey of the current node
	 * @param node
	 *        the node to create the tooltip for
	 * @return the tooltip to display in the chart, null permitted
	 */
	protected String generateTooltip(DataKey dataKey, ChartNode node) {
		return dataKey + ": " + MetaLabelProvider.INSTANCE.getLabel(node.getValue());
	}

	/**
	 * Factory for {@link DefaultTooltipGenerator}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class Provider implements XYToolTipGeneratorProvider, PieToolTipGeneratorProvider,
			CategoryToolTipGeneratorProvider, XYZToolTipGeneratorProvider {

		/** Singleton {@link Provider} instance. */
		public static final Provider INSTANCE = new Provider();

		/**
		 * Creates a new {@link Provider}.
		 */
		protected Provider() {
			// singleton instance
		}

		@Override
		public XYToolTipGenerator getXYTooltipGenerator(JFreeChart model, ChartContext context,
				ChartData<? extends AbstractSeriesDataset> chartData) {
			return newInstance(chartData);
		}

		@Override
		public PieToolTipGenerator getPieTooltipGenerator(JFreeChart model, ChartContext context,
				ChartData<? extends PieDataset> chartData) {
			return newInstance(chartData);
		}

		@Override
		public CategoryToolTipGenerator getCategoryTooltipGenerator(JFreeChart model, ChartContext context,
				ChartData<? extends CategoryDataset> chartData) {
			return newInstance(chartData);
		}

		/**
		 * Creates a {@link DefaultTooltipGenerator} with corresponding context.
		 */
		protected DefaultTooltipGenerator newInstance(ChartData<? extends Dataset> chartData) {
			return new DefaultTooltipGenerator(chartData);
		}

		@Override
		public XYZToolTipGenerator getXYZTooltipGenerator(JFreeChart model, ChartContext context,
				ChartData<? extends AbstractSeriesDataset> chartData) {
			return new DefaultTooltipGenerator(chartData);
		}

	}

}