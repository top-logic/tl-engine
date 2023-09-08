/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter;

import java.util.Collection;

import com.top_logic.basic.col.Filter;

/**
 * {@link Filter} accepting <code>null</code> or empty {@link Collection}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NoValueFilter implements Filter<Object> {

	/**
	 * Singleton {@link NoValueFilter} instance.
	 */
	public static final NoValueFilter INSTANCE = new NoValueFilter();

	private NoValueFilter() {
		// Singleton constructor.
	}

	@Override
	public boolean accept(Object anObject) {
		if (anObject == null) {
			return true;
		}
		if (anObject instanceof Collection<?>) {
			return ((Collection<?>) anObject).isEmpty();
		}
		if (anObject instanceof String) {
			return ((String) anObject).trim().isEmpty();
		}
		return false;
	}

}
