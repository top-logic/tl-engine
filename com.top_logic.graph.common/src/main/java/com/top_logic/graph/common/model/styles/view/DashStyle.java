/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model.styles.view;

/**
 * Dash style for edges.
 * 
 * @see Stroke#getDashStyle()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum DashStyle {

	/**
	 * A solid line.
	 */
	SOLID,

	/**
	 * A dashed line.
	 */
	DASH,

	/**
	 * A line with alternating dashes and dots.
	 */
	DASH_DOT,

	/**
	 * A line with alternating dashes and dots.
	 */
	DASH_DOT_DOT,

	/**
	 * A dotted line.
	 */
	DOT,

	;

}
