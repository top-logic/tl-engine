/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

/**
 * Positioning strategy for insertion into lists.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum PositionStrategy {
	/**
	 * Specifies a position at the start of a list.
	 */
	START,
	
	/**
	 * Specifies a position at the end of a list.
	 */
	END,

	/**
	 * Specifies a position before a context element.
	 */
	BEFORE, 
	
	/**
	 * Specifies a position after a context element.
	 */
	AFTER,
	
	/**
	 * Specifies a position w.r.t. the sort order of a list.
	 */
	AUTO;
}
