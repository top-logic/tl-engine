/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.rendering.chart;

import java.util.List;

import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.reporting.flex.chart.config.dataset.AbstractDatasetBuilder;
import com.top_logic.reporting.flex.chart.config.dataset.DatasetBuilder;
import com.top_logic.reporting.flex.chart.config.model.ChartNode;
import com.top_logic.reporting.flex.chart.config.model.ChartTree;
import com.top_logic.reporting.flex.chart.config.util.Configs.YHighLowInterval;

/**
 * {@link DatasetBuilder}-implementation for performance-chart.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class YIntervalSeriesBuilder extends AbstractDatasetBuilder<YIntervalSeriesCollection> {

	/**
	 * Config-constructor for a {@link YIntervalSeriesBuilder}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public YIntervalSeriesBuilder(InstantiationContext context, AbstractDatasetBuilder.Config config) {
		super(context, config);
	}

	@Override
	public Class<YIntervalSeriesCollection> getDatasetType() {
		return YIntervalSeriesCollection.class;
	}

	@Override
	protected YIntervalSeriesCollection internalCreateDataset(ChartTree tree) {

		YIntervalSeriesCollection result = new YIntervalSeriesCollection();
		
		List<ChartNode> children = tree.getRoot().getChildren();
		for (ChartNode node : children) {
			Comparable<?> key = node.getKey();
			YIntervalSeries series = new YIntervalSeries(key);
			result.addSeries(series);
			List<YHighLowInterval> objects =
				CollectionUtil.dynamicCastView(YHighLowInterval.class, node.getObjects());
			for (YHighLowInterval interval : objects) {
				series.add(interval.getX(), interval.getY(), interval.getYLow(), interval.getYHigh());
			}
		}

		return result;
	}

}