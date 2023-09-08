/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.color;

import java.awt.Color;

/**
 * {@link ColorContext} whose color assignment can be updated.
 */
public interface ModifiableColorContext extends ColorContext {

	/**
	 * Assigns a new color to the given value.
	 * 
	 * @param key
	 *        The value to assign the color to.
	 * @param col
	 *        The new {@link Color} to use for the given value.
	 */
	void setColor(Object key, Color col);

	/**
	 * Resets all assignments.
	 */
	void reset();

}