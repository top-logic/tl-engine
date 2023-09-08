/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.color;

import java.awt.Paint;

/**
 * Assigns colors to model objects.
 * 
 * <p>
 * If the same {@link ColorContext} is asked multiple times to provide a color for the same object,
 * it should always return the same color.
 * </p>
 * 
 * @see ColorProvider
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public interface ColorContext {

	/**
	 * @param obj
	 *        The model object to assign a color to.
	 * @return The color to be used for rendering chart regions associated to the given object.
	 */
	public Paint getColor(Object obj);

}