/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.equal;

/**
 * {@link EqualitySpecification} that uses the target objects {@link #equals(Object)} and
 * {@link #hashCode()} implementations.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class ValueEqualitySpecification extends EqualitySpecification<Object> {

	/** The singleton {@link ValueEqualitySpecification} instance. */
	public static final ValueEqualitySpecification INSTANCE = new ValueEqualitySpecification();

	private ValueEqualitySpecification() {
		// Reduce visibility
	}

	@Override
	protected boolean equalsInternal(Object left, Object right) {
		return left.equals(right);
	}

	@Override
	protected int hashCodeInternal(Object object) {
		return object.hashCode();
	}

}
