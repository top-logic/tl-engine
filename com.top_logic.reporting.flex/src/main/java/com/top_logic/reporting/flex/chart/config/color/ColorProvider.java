/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.color;


/**
 * Factory to create {@link ColorContext} instances for rendering a charts.
 * 
 * <p>
 * The {@link ColorContext} should be released at latest with the session because it may keep
 * references to objects that have been asked for color.
 * </p>
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public interface ColorProvider {

	/**
	 * Creates a new instance of {@link ColorContext} to use during the rendering to get colors for
	 * the chart.
	 */
	public ColorContext createColorContext();

}
