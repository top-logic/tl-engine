/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder.tree.icon;

import java.awt.Image;
import java.util.Map;

import com.top_logic.reporting.flex.chart.config.chartbuilder.tree.TreeAxisTimeSeriesChartBuilder;
import com.top_logic.reporting.flex.chart.config.model.ChartNode;

/**
 * Used for {@link TreeAxisTimeSeriesChartBuilder} to provide images for the values.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public interface ValueIconProvider {

	/**
	 * the map icon by key
	 */
	public Map<Integer, Image> getIcons();

	/**
	 * @param node
	 *        the chart-node identifying the position in the chart
	 * @return the icon to visualize the value
	 */
	public Image getIcon(ChartNode node);

}