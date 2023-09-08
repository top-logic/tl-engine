/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model.geometry;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.DoubleDefault;

/**
 * Thickness of insets.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Insets {

	/**
	 * @see #getLeft()
	 */
	String LEFT = "left";

	/**
	 * @see #getTop()
	 */
	String TOP = "top";

	/**
	 * @see #getRight()
	 */
	String RIGHT = "right";

	/**
	 * @see #getBottom()
	 */
	String BOTTOM = "bottom";

	/**
	 * Gets the top inset.
	 */
	@Name(TOP)
	@DoubleDefault(5.0)
	double getTop();

	/**
	 * Gets the left inset.
	 */
	@Name(LEFT)
	@DoubleDefault(5.0)
	double getLeft();

	/**
	 * Gets the bottom inset.
	 */
	@Name(BOTTOM)
	@DoubleDefault(5.0)
	double getBottom();

	/**
	 * Gets the right inset.
	 */
	@Name(RIGHT)
	@DoubleDefault(5.0)
	double getRight();

}
