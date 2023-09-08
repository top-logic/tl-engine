/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.func;

/**
 * A two-argument function.
 * 
 * <p>
 * Must be implemented through {@link Function2}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface IFunction2<R, A1, A2> {

	/**
	 * Implementation of the two argument function.
	 */
	R apply(A1 arg1, A2 arg2);

}
