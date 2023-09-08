/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values;

/**
 * A three-argument function.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Function3<A1, A2, A3, R> {

	/**
	 * Produce the function result.
	 * 
	 * @param arg1
	 *        The first argument.
	 * @param arg2
	 *        The second argument.
	 * @param arg3
	 *        The third argument.
	 * @return The function result.
	 */
	R eval(A1 arg1, A2 arg2, A3 arg3);

}
