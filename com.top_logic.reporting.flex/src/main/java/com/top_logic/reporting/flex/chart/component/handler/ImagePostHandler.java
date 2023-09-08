/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.component.handler;

import java.awt.Dimension;
import java.util.Map;

import com.top_logic.base.chart.component.JFreeChartComponent;
import com.top_logic.layout.DisplayContext;
import com.top_logic.reporting.flex.chart.component.AbstractChartComponent;
import com.top_logic.reporting.flex.chart.config.ChartConfig;

/**
 * Interface for custom modification while preparing the image and after the config was set.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public interface ImagePostHandler {

	/**
	 * Called in
	 * {@link JFreeChartComponent#prepareImage(com.top_logic.layout.DisplayContext, String, Dimension)}
	 * e.g. to modify the dimension.
	 */
	@SuppressWarnings("rawtypes")
	public void prepareImage(AbstractChartComponent comp, DisplayContext aContext, String anImageId,
			Dimension aDimension, Map dimensions, String aKey);

	/**
	 * Called after the {@link ChartConfig} was set and the chart was initialized to update
	 * dependent components
	 */
	public void postSetConfig(AbstractChartComponent component);

}