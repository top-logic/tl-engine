/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.top_logic.basic.UnreachableAssertion;

/**
 * {@link Iterator} that allows to apply a {@link #test(Object) filter},
 * {@link #transform(Object) map}, {@link #acceptDestination(Object) filter}
 * operation chain on a source {@link Iterator}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class TransformIterator<S, D> implements Iterator<D> {

	/**
	 * There is no element in {@link #buffer}, i.e. neither {@link Iterator#hasNext()} nor
	 * {@link Iterator#next()} was called, or {@link Iterator#remove()} was called.
	 */
	private static final byte INITIAL = 0;

	/**
	 * An element was returned, i.e. the previous call was {@link Iterator#next()}.
	 */
	private static final byte OFFERED = 1;

	/**
	 * A next element is in {@link #buffer}.
	 * 
	 * <p>
	 * {@link Iterator#hasNext()} of source iterator was called and the pointer has been moved
	 * forward.
	 * </p>
	 */
	private static final byte BUFFERED = 2;

	/**
	 * There is no element in {@link #buffer} and the {@link #source} is exhausted.
	 * 
	 * <p>
	 * {@link Iterator#hasNext()} of source iterator was called and the pointer has been moved
	 * forward.
	 * </p>
	 */
	private static final byte EXHAUSTED = 3;

	/**
	 * The source {@link Iterator} from which values are taken.
	 */
	private final Iterator<? extends S> source;

	/**
	 * One of {@link #INITIAL}, {@link #BUFFERED}, {@link #OFFERED}, or
	 * {@link #EXHAUSTED}.
	 */
	private byte state = INITIAL;

	/**
	 * The last buffered element taken from {@link #source}.
	 */
	private D buffer;

	/**
	 * Creates a {@link TransformIterator}.
	 * 
	 * @param source
	 *        The source {@link Iterator} to take values from.
	 */
	public TransformIterator(Iterator<? extends S> source) {
		this.source = source;
	}

	@Override
	public boolean hasNext() {
		switch (state) {
		case INITIAL:
		case OFFERED:
			return findNext();
		case BUFFERED:
			return true;
		case EXHAUSTED:
			return false;
		default:
			throw new UnreachableAssertion("No such state: " + state);
		}
	}

	@Override
	public D next() {
		switch (state) {
		case INITIAL:
		case OFFERED:
			if (findNext()) {
				return deliver();
			} else {
				throw new NoSuchElementException();
			}
		case BUFFERED:
			return deliver();
		case EXHAUSTED:
			throw new NoSuchElementException();
		default:
			throw new UnreachableAssertion("No such state: " + state);
		}
	}

	@Override
	public void remove() {
		switch (state) {
		case INITIAL:
			throw new IllegalStateException("Next was not called.");
		case OFFERED:
			state = INITIAL;
			source.remove();
			return;
		case BUFFERED:
		case EXHAUSTED:
				/* There is a general problem in call sequence next(), hasNext(), remove(). The call
				 * hasNext() causes to iterate to next element to check. Then a call to remove()
				 * would remove not the currently offered, but the next element. */
				throw new IllegalStateException(
					"Can not remove item directly after hasNext(), because it is necessary to switch the underlying iterator forward.");
		default:
			throw new UnreachableAssertion("No such state: " + state);
		}
	}
	
	/**
	 * Fetches and buffers the next accepted element from the {@link #source}.
	 * 
	 * @return Whether a next element was found.
	 */
	private boolean findNext() {
		while (source.hasNext()) {
			S sourceValue = source.next();
			if (test(sourceValue)) {
				D destinationValue = transform(sourceValue);
				if (acceptDestination(destinationValue)) {
					buffer = destinationValue;
					state = BUFFERED;
					return true;
				}
			}
		}
		buffer = null;
		state = EXHAUSTED;
		return false;
	}

	/**
	 * Removes the last element from {@link #buffer}.
	 * 
	 * @return the element to deliver.
	 */
	private D deliver() {
		state = OFFERED;
		return buffer;
	}

	/**
	 * Algorithm that decides about acceptance of a source value directly taken
	 * from the source {@link Iterator}.
	 * 
	 * @param value
	 *        The source value.
	 * @return Whether processing should continue with that value.
	 */
	protected abstract boolean test(S value);

	/**
	 * Transforms an {@link #test(Object) accepted} source value into a
	 * destination value.
	 * 
	 * @param value
	 *        The source value.
	 * @return The destination value.
	 */
	protected abstract D transform(S value);

	/**
	 * Algorithm that decides about acceptance of a {@link #transform(Object)
	 * transformed} destination value.
	 * 
	 * @param value
	 *        The destination value.
	 * @return Whether the transformed value should be returned from this
	 *         {@link Iterator}.
	 */
	protected abstract boolean acceptDestination(D value);

}
