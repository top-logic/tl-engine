/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder.tree.icon;

import java.awt.Image;

import com.top_logic.reporting.common.tree.TreeAxis;
import com.top_logic.reporting.flex.chart.config.chartbuilder.tree.TreeAxisTimeSeriesChartBuilder;

/**
 * Used for {@link TreeAxisTimeSeriesChartBuilder} to provide icons for the {@link TreeAxis}
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public interface LabelIconProvider {

	/**
	 * an array of images used as icons in {@link TreeAxis}. The icons are identified by
	 *         their index.
	 */
	public Image[] getIcons();

}