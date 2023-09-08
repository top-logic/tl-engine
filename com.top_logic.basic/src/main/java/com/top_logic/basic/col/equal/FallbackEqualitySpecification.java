/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.equal;

/**
 * An {@link EqualitySpecification} which has a fallback for objects which are not supported by the
 * main {@link EqualitySpecification}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class FallbackEqualitySpecification<F, M extends F> extends EqualitySpecification<F> {

	private final Class<M> _type;

	private final EqualitySpecification<? super M> _main;

	private final EqualitySpecification<? super F> _fallback;

	/**
	 * Create a {@link FallbackEqualitySpecification}.
	 * 
	 * @param type
	 *        Only objects of this type are handled by the given main {@link EqualitySpecification},
	 *        all others are handled by the fallback.
	 * @param main
	 *        {@link EqualitySpecification} for objects of the given type.
	 * @param fallback
	 *        {@link EqualitySpecification} used for all other objects.
	 */
	public FallbackEqualitySpecification(Class<M> type, EqualitySpecification<? super M> main,
			EqualitySpecification<? super F> fallback) {

		// Fail early
		checkParametersNotNull(type, main, fallback);

		_type = type;
		_main = main;
		_fallback = fallback;
	}

	private void checkParametersNotNull(Class<M> type, EqualitySpecification<? super M> main,
			EqualitySpecification<? super F> fallback) {
		if (type == null) {
			throw new NullPointerException();
		}
		if (main == null) {
			throw new NullPointerException();
		}
		if (fallback == null) {
			throw new NullPointerException();
		}
	}

	@Override
	protected boolean equalsInternal(F left, F right) {
		if (_type.isInstance(left) && _type.isInstance(right)) {
			return _main.equals(_type.cast(left), _type.cast(right));
		}
		return _fallback.equals(left, right);
	}

	@Override
	protected int hashCodeInternal(F object) {
		if (_type.isInstance(object)) {
			return _main.hashCode(_type.cast(object));
		}
		return _fallback.hashCode(object);
	}

}
