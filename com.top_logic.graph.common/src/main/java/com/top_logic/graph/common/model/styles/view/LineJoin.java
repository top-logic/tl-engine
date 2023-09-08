/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model.styles.view;

/**
 * Line join style.
 * 
 * @see Stroke#getLineJoin()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum LineJoin {

	/**
	 * Beveled line join.
	 */
	BEVEL,

	/**
	 * Sharp line join.
	 */
	MITER,

	/**
	 * Rounded line join.
	 */
	ROUND,

	;

}
