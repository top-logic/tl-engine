/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.boundsec.manager.rule;

import java.util.Objects;

/**
 * Wrapping object for elements that are potential base objects of a {@link RoleRule} or
 * {@link PathElement}.
 * 
 * <p>
 * There is a special {@link BaseObjects} instance, {@link BaseObjects#all()}, that represent that
 * it is impossible to determine base objects for the {@link RoleRule} or {@link PathElement}.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract sealed class BaseObjects<T> {

	@SuppressWarnings("rawtypes")
	private static final BaseObjects ALL = new All();

	/**
	 * Representation that "all" objects are potential base objects for the {@link RoleRule} or
	 * {@link PathElement}.
	 */
	@SuppressWarnings("unchecked")
	public static <T> BaseObjects<T> all() {
		return ALL;
	}

	/**
	 * Creates a new {@link BaseObjects} object wrapping the given base objects.
	 * 
	 * @param objects
	 *        The objects that are potential base objects of a {@link RoleRule} or
	 *        {@link PathElement}.
	 */
	public static <T> BaseObjects<T> of(T objects) {
		return new Impl<>(objects);
	}

	/**
	 * Access to the wrapped value.
	 * 
	 * <p>
	 * The value must only be accessed when this object is <b>not</b> {@link #all()}.
	 * </p>
	 */
	public abstract T get();

	/**
	 * Whether this {@link BaseObjects} object is {@link #all()}.
	 */
	public abstract boolean isAll();

	private static final class All<T> extends BaseObjects<T> {

		@Override
		public T get() {
			throw new IllegalStateException("Can not determine \"all\" base objects.");
		}

		@Override
		public boolean isAll() {
			return true;
		}

		@Override
		public String toString() {
			return "all()";
		}
	}

	private static final class Impl<T> extends BaseObjects<T> {

		private final T _value;

		private Impl(T value) {
			_value = value;
		}

		@Override
		public T get() {
			return _value;
		}

		@Override
		public boolean isAll() {
			return false;
		}

		@Override
		public String toString() {
			return "value: " + get();
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(_value);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			return obj instanceof Impl other
					&& Objects.equals(_value, other._value);
		}

	}
}

