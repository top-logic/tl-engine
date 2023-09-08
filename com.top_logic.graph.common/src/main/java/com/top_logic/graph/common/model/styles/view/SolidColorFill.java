/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model.styles.view;

import com.top_logic.basic.config.annotation.Name;

/**
 * Fills an area with a specified solid color.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface SolidColorFill extends Fill {

	/**
	 * Gets the solid color with which the area is filled.
	 */
	@Name("color")
	Color getColor();

}
