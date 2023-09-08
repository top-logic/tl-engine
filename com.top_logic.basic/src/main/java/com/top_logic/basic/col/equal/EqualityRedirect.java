/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.equal;

import java.util.Map;
import java.util.Set;

/**
 * Used to store objects in a {@link Set} or {@link Map}, but with a different "equality
 * specification".
 * <p>
 * Objective: Storing objects in a {@link Set} or {@link Map}, but with a different implementation
 * for {@link Object#equals(Object)} and {@link Object#hashCode()} than they implement. <br/>
 * Problem: There is no API in Java collections for this. <br/>
 * Solution: {@link EqualityRedirect}s store the objects but redirects the
 * {@link Object#equals(Object)} and {@link Object#hashCode()} methods to an external
 * {@link EqualitySpecification}.
 * </p>
 * <p>
 * Only {@link EqualityRedirect}s with <em>the same</em> {@link EqualitySpecification} instance can
 * be equal. This is checked with '=='. Therefore, overriding
 * {@link EqualitySpecification#equals(Object)} has no effect for the {@link EqualityRedirect}.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class EqualityRedirect<T> {

	private final T _value;

	private final EqualitySpecification<? super T> _equalsSpec;

	/**
	 * Creates an {@link EqualityRedirect}.
	 */
	public EqualityRedirect(T value, EqualitySpecification<? super T> equalsSpec) {
		_value = value;
		_equalsSpec = equalsSpec;
	}

	/**
	 * The value whose equality specification is being overridden.
	 */
	public T getValue() {
		return _value;
	}

	/**
	 * The new {@link EqualitySpecification} for the {@link #getValue() value}.
	 */
	public EqualitySpecification<? super T> getEqualitySpecification() {
		return _equalsSpec;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof EqualityRedirect)) {
			return false;
		}
		EqualityRedirect<?> otherRedirect = (EqualityRedirect<?>) other;
		if (getEqualitySpecification() != otherRedirect.getEqualitySpecification()) {
			// If the EqualitySpecifications don't match, they cannot be used,
			// as a.equals(b) == b.equals(a) is not guaranteed in that case.
			// Therefore, EqualityRedirects aren't equal if they don't have the same EqualitySpecification.
			return false;
		}
		// The other EqualityRedirect would ask the same EqualitySpecification instance with its
		// value. Therefore, this cast is safe.
		@SuppressWarnings("unchecked")
		T otherValue = (T) otherRedirect.getValue();
		return getEqualitySpecification().equals(getValue(), otherValue);
	}

	@Override
	public int hashCode() {
		return getEqualitySpecification().hashCode(_value);
	}

}
