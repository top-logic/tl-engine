/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder.tree.icon;

import java.awt.Image;

/**
 * Provider for an image to use in charts.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public interface IconProvider {

	/**
	 * the image to use to visualize a value in a chart
	 */
	public Image getImage();
}