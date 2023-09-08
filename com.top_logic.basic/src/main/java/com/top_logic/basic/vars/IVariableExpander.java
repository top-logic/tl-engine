/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.vars;

/**
 * Algorithm expanding variables in strings.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface IVariableExpander {

	/**
	 * Expands variables in the given string and returns the expansion result.
	 */
	String expand(String s);

}

