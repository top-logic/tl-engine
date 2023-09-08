/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.function.Predicate;

/**
 * Basic filter interface for filtering objects.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public interface Filter<T> extends Predicate<T> {

    /**
	 * Check, whether the given object is accepted by this {@link Filter}.
	 * 
	 * @param obj
	 *        The object to be checked.
	 * @return <code>true</code>, if object is accepted by this filter.
	 */
	public boolean accept(T obj);
    
	@Override
	default boolean test(T t) {
		return accept(t);
	}
}
