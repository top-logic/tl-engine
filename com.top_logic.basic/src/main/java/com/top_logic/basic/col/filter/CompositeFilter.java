/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter;

import com.top_logic.basic.col.Filter;

/**
 * {@link Filter} that is composed of potentially multiple single {@link Filter}
 * implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface CompositeFilter<T> extends Filter, Iterable<T> {

	/**
	 * The number of composed {@link Filter}s.
	 */
	int size();

	/**
	 * The composed {@link Filter} at the given index.
	 */
	Filter getFilter(int n);
	
}
