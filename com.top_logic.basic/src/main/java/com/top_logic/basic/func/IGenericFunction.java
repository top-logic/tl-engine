/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.func;

/**
 * Multi-argument function.
 * 
 * <p>
 * Must be implemented through {@link GenericFunction}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface IGenericFunction<R> {

	/**
	 * Invokes this function through a generic interface.
	 * 
	 * @param args
	 *        The function arguments.
	 * @return The function result.
	 */
	R invoke(Object... args);

}
