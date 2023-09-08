/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.treexf;

/**
 * {@link Node} representing a primitive value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Value extends Node {

	/**
	 * The represented primitive value.
	 * 
	 * @param match
	 *        Current bindings of the current match.
	 * @return The represented value.
	 */
	Object getValue(Match match);

}
