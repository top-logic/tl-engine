/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.equal;

import java.util.function.Function;

/**
 * Computes equality using the {@link Object#equals(Object)} on the results of a given
 * mapping-{@link Function}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class CustomEqualitySpecification<T> extends EqualitySpecification<T> {

	private Function<T, ?> _equalsBy;

	/**
	 * Creates a {@link CustomEqualitySpecification}. Checks equality of the mapped values.
	 * 
	 * @param equalsBy
	 *        Function that is applied to both of the values passed to
	 *        {@link #equals(Object, Object)}. The values are considered equal, if the resulting
	 *        values are {@link Object#equals(Object) equal}. The function must not return
	 *        <code>null</code> for any value.
	 */
	public CustomEqualitySpecification(Function<T, ?> equalsBy) {
		_equalsBy = equalsBy;
	}

	@Override
	protected boolean equalsInternal(T left, T right) {
		return _equalsBy.apply(left).equals(_equalsBy.apply(right));
	}

	@Override
	protected int hashCodeInternal(T object) {
		return 0;
	}

}
