/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;


/**
 * Mapping of arbitrary values to {@link Flavor}s.
 *
 * @param <T> The value type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Style<T> {
	
	/**
	 * Find a matching value for the given {@link Flavor}.
	 * 
	 * @param flavor
	 *        The {@link Flavor} to search a value for.
	 * @return The best matching value of the given {@link Flavor}.
	 * 
	 * @see com.top_logic.basic.col.Mapping#map(java.lang.Object)
	 */
    public T getValue(Flavor flavor);
	
}
