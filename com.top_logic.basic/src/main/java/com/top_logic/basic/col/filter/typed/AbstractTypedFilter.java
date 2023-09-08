/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter.typed;

/**
 * The default base class for {@link TypedFilter}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class AbstractTypedFilter<T> implements TypedFilter {

	// The warning can be ignored, as the type is checked explicitly.
	@SuppressWarnings("unchecked")
	@Override
	public final FilterResult matches(Object object) {
		if (object == null) {
			return matchesNull();
		}
		if (!isApplicable(object)) {
			return FilterResult.INAPPLICABLE;
		}
		return matchesTypesafe((T) object);
	}

	/**
	 * Whether null is matched by this filter.
	 * <p>
	 * Hook for subclasses. Default is {@link FilterResult#INAPPLICABLE} as "null instanceof Object"
	 * is false.
	 * </p>
	 */
	protected FilterResult matchesNull() {
		return FilterResult.INAPPLICABLE;
	}

	private boolean isApplicable(Object object) {
		return getType().isInstance(object);
	}

	/**
	 * Is called by {@link #matches(Object)} if the object is an instance of {@link #getType()}.
	 * 
	 * @param object
	 *        Is never null. See: {@link #matchesNull()}
	 */
	protected abstract FilterResult matchesTypesafe(T object);

}
