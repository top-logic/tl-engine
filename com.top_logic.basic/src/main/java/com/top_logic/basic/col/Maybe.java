/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.NoSuchElementException;

/**
 * {@link Maybe} is a box for a value, making the decision explicit, what to do if there is no value.
 * (And forcing users of the value to think about the alternatives!)
 * You can choose to:
 * <ul>
 *   <li>Get the value and get an Exception thrown if there is none: {@link #getElseError()}</li>
 *   <li>Get the value and provide a fallback in case there is none: {@link #getElse(Object)}</li>
 *   <li>Explicitly ask if there is a value: {@link #hasValue()}</li>
 * </ul>
 * 
 * To construct a {@link Maybe}, call {@link #toMaybe(Object)} with the value.
 * It treats <code>null</code> as "there is no value". Alternatively, if you already know that there is no value,
 * you can directly call {@link #none()}.
 * 
 * Example usage:
 * 
 * <xmp>
 * public Maybe<String> getLabel(Object object) {
 *   String label = lookupLabel(object); // label may be null if it wasn't defined
 *   return Maybe.toMaybe(label);
 * }
 * 
 * // Somewhere else:
 * label = labelProvider.getLabel(someObject).getElseError(); //If a missing label is a failure, express that!
 * 
 * // Somewhere else:
 * // Not every label is customized:
 * label = labelProviderForCustomizedLabels.getLabel(someObject).getElse(defaultLabel);
 * </xmp>
 * 
 * @author    <a href=mailto:Jan.Stolzenburg@top-logic.com>Jan.Stolzenburg</a>
 */
public abstract class Maybe<T> {

	/**
	 * The singleton {@link Maybe} representing "there is no value".
	 * 
	 * @author    <a href=mailto:Jan.Stolzenburg@top-logic.com>Jan.Stolzenburg</a>
	 */
	private static final class None<S> extends Maybe<S> {

		/**
		 * Singleton {@link Maybe.None} instance.
		 */
		public static final None<Object> INSTANCE = new None<>();

		private None() {
			/* Private singleton constructor */
		}

		@Override
		public boolean hasValue() {
			return false;
		}

		@Override
		public S getElse(S fallbackValue) {
			return fallbackValue;
		}

		@Override
		public S getElseError() throws NoSuchElementException {
			throw new NoSuchElementException("There is no value.");
		}

		@Override
		public String toString() {
			return "Maybe.none()";
		}

	}

	/**
	 * The {@link Maybe} representing "there is some value".
	 * 
	 * @author    <a href=mailto:Jan.Stolzenburg@top-logic.com>Jan.Stolzenburg</a>
	 */
	private static final class Some<S> extends Maybe<S> {

		private final S value;

		Some(S value) {
			this.value = value;
		}

		@Override
		public boolean hasValue() {
			return true;
		}

		@Override
		public S getElse(S fallbackValue) {
			return value;
		}

		@Override
		public S getElseError() {
			return value;
		}

		@Override
		public boolean equals(Object other) {
	    	if (other == this) {
	    		return true;
	    	}
			if (!(other instanceof Some)) {
				return false;
			}
			Some<?> otherSome = (Some<?>) other;
			if (value == null) {
				return otherSome.value == null;
			} else {
				return value.equals(otherSome.value);
			}
		}

		@Override
		public int hashCode() {
			if (value == null) {
				return 12345679;
			} else {
				return 12345679 ^ value.hashCode();
			}
		}

		@Override
		public String toString() {
			return "Maybe.some(" + value + ")";
		}

	}

	Maybe() {
		/* Hide constructor */
	}

	/**
	 * Has this {@link Maybe} a value?
	 */
	public abstract boolean hasValue();

	/**
	 * If this {@link Maybe} has a value, it is returned.
	 * Otherwise, the given fallback value is returned.
	 */
	public abstract T getElse(T fallbackValue);

	/**
	 * The value of the {@link Maybe}.
	 * 
	 * @throws NoSuchElementException If this {@link Maybe} has no value.
	 */
	public abstract T getElseError() throws NoSuchElementException;

	@Override
	public abstract String toString();

	/**
	 * The value of the {@link Maybe}.
	 * 
	 * <p>Same as {@link #getElseError()} to be used in a context, where the surrounding code makes
	 * sure that no error may occur.</p>
	 * 
	 * @throws NoSuchElementException If this {@link Maybe} has no value.
	 */
	public final T get() throws NoSuchElementException {
		return getElseError();
	}

	/**
	 * Always returns a {@link Maybe} without value. (The singleton {@link None}).
	 * Equivalent to <code>toMaybe(null)</code>
	 */
	@SuppressWarnings("unchecked")
	public static <T> Maybe<T> none() {
		return (Maybe<T>) None.INSTANCE;
	}

	/**
	 * If the given value is <code>null</code>, a {@link Maybe} without value is returned. (The singleton {@link None}).
	 * If it is not <code>null</code>, a {@link Maybe} with the given value is returned.
	 */
	public static <T> Maybe<T> toMaybe(T value) {
		if (value == null) {
			return none();
		}
		else {
			return new Some<>(value);
		}
	}

	/**
	 * Create a {@link Maybe} for the given non-<code>null</code> value.
	 * 
	 * @param <T>
	 *        The value type.
	 * @param nonNullValue
	 *        The non-<code>null</code> value to wrap.
	 * @return A {@link Maybe} that is guaranteed to have a value.
	 * 
	 * @throws IllegalArgumentException
	 *         If the given value is null.
	 */
	public static <T> Maybe<T> some(T nonNullValue) {
		if (nonNullValue == null) {
			throw new IllegalArgumentException("Value must not be null.");
		}
		else {
			return new Some<>(nonNullValue);
		}
	}
	
	/**
	 * Always returns a {@link Maybe} with the given value, even if the value is <code>null</code>.
	 * You should only use this method if <code>null</code> is a valid business object
	 * and does not stand for "no value". Therefore, it's best if you don't use it at all.
	 * The ugly long name of this method should emphasize that.
	 */
	public static <T> Maybe<T> toMaybeButTreatNullAsValidValue(T value) {
		return new Some<>(value);
	}

}
