/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter.typed;

import com.top_logic.basic.col.Filter;

/**
 * A filter that can be asked for its type parameter: {@link #getType()}.
 * <p>
 * This enables to check externally whether a filter can process a given object. This is necessary
 * when filters are used untyped, for example in the configuration.
 * </p>
 * <p>
 * If a {@link TypedFilter} is applied to an object that it does not support, it returns
 * {@link FilterResult#INAPPLICABLE}. This is useful for example in an AND filter which inner
 * filters don't support the given object.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface TypedFilter {

	/**
	 * The {@link Class} representing the type supported by this class.
	 * <p>
	 * It is used to check at runtime whether an object can be filtered with this {@link Filter}. If
	 * not, the filter returns {@link FilterResult#INAPPLICABLE}.
	 * </p>
	 */
	Class<?> getType();

	/**
	 * @param object
	 *        Is allowed to be null. Whether null results in {@link FilterResult#INAPPLICABLE} is up
	 *        to the concrete implementation.
	 * 
	 * @return Never null.
	 */
	FilterResult matches(Object object);

}
