/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model.styles.view;

import com.top_logic.graph.common.model.styles.PolylineEdgeStyle;

/**
 * Predefined arrow styles.
 * 
 * @see PolylineEdgeStyle#getTargetArrow()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum Arrow {

	/**
	 * No arrow.
	 */
	NONE,

	/**
	 * A regular arrow.
	 */
	DEFAULT,

	/**
	 * A simple arrow.
	 */
	SIMPLE,

	/**
	 * No short default arrow.
	 */
	SHORT,

	/**
	 * A diamond.
	 */
	DIAMOND,

	/**
	 * A cross.
	 */
	CROSS,

	/**
	 * A circle.
	 */
	CIRCLE,

	/**
	 * A filled arrow.
	 */
	TRIANGLE,

	;

}
