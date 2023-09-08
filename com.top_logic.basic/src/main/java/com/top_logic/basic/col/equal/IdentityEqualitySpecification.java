/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.equal;

/**
 * {@link EqualitySpecification} that uses the '==' and {@link System#identityHashCode(Object)}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class IdentityEqualitySpecification extends EqualitySpecification<Object> {

	/** The singleton {@link IdentityEqualitySpecification} instance. */
	public static final IdentityEqualitySpecification INSTANCE = new IdentityEqualitySpecification();

	private IdentityEqualitySpecification() {
		// Reduce visibility
	}

	@Override
	protected boolean equalsInternal(Object left, Object right) {
		return left == right;
	}

	@Override
	protected int hashCodeInternal(Object object) {
		return System.identityHashCode(object);
	}

}
