/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.equal;

import java.util.Map;
import java.util.Set;

/**
 * Encapsulates an "equality specification".
 * <p>
 * Objective: Storing objects in a {@link Set} or {@link Map}, but with a different implementation
 * for {@link Object#equals(Object)} and {@link Object#hashCode()} than they implement. <br/>
 * Problem: There is no api in the Java collections for this. <br/>
 * Solution: {@link EqualityRedirect}s store the objects but redirects the
 * {@link Object#equals(Object)} and {@link Object#hashCode()} methods to an external
 * {@link EqualitySpecification}. <br/>
 * (Short: A workaround for Java insufficiencies.) <br/>
 * Only {@link EqualityRedirect} with <em>the same</em> {@link EqualitySpecification} instance can
 * be equal. This is checked with '=='. Therefore, overriding
 * {@link EqualitySpecification#equals(Object)} has no effect for the {@link EqualityRedirect}.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class EqualitySpecification<T> {

	/**
	 * Provides an alternative implementation for {@link Object#equals(Object)}.
	 * <p>
	 * Both parameters are allowed to be <code>null</code>.
	 * </p>
	 */
	public final boolean equals(T left, T right) {
		if (left == right) {
			// Invariant for all implementations, as it is requested by the equals contract.
			return true;
		}
		if (left == null || right == null) {
			// Invariant for all implementations, as it is requested by the equals contract.
			return false;
		}
		return equalsInternal(left, right);
	}

	/** Both parameters are never <code>null</code> and never the same. */
	protected abstract boolean equalsInternal(T left, T right);

	/**
	 * Provides an alternative implementation for {@link Object#hashCode()}.
	 * 
	 * @param object
	 *        Is allowed to be null.
	 */
	public final int hashCode(T object) {
		if (object == null) {
			// As defined by System.identityHashCode(null)
			return 0;
		}
		return hashCodeInternal(object);
	}

	/**
	 * @param object
	 *        Is never <code>null</code>.
	 */
	protected abstract int hashCodeInternal(T object);

	/**
	 * Overridden to declare it 'final' to ensure nobody misunderstands something and overrides this
	 * instead of (or in addition to) {@link #equalsInternal(Object, Object)}.
	 */
	@Override
	public final boolean equals(Object obj) {
		return super.equals(obj);
	}

	/**
	 * Overridden to declare it 'final' to ensure nobody misunderstands something and overrides this
	 * instead of (or in addition to) {@link #hashCodeInternal(Object)}.
	 */
	@Override
	public final int hashCode() {
		return super.hashCode();
	}

}
