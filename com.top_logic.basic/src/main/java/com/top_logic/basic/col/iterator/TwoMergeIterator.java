/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.iterator;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.top_logic.basic.NamedConstant;

/**
 * Iterator that merges two iterators in comparator order.
 * 
 * @see IteratorUtil#mergeIterators(Comparator, java.util.List)
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TwoMergeIterator<E> implements Iterator<E> {

	private static final Object EMPTY = new NamedConstant("Empty");

	private final Comparator<? super E> _comparator;

	private final Iterator<? extends E> _it1, _it2;

	private E _val1, _val2;

	/**
	 * Creates a {@link TwoMergeIterator}.
	 *
	 * @param comparator
	 *        Comparator defining the order of the result iterator.
	 * @param it1
	 *        First iterator. Must be return its values in {@link Comparator} order.
	 * @param it2
	 *        Second iterator. Must be return its values in {@link Comparator} order.
	 */
	public TwoMergeIterator(Comparator<? super E> comparator, Iterator<? extends E> it1, Iterator<? extends E> it2) {
		_comparator = comparator;
		_it1 = it1;
		_it2 = it2;
		fillVal1();
		fillVal2();
	}

	@SuppressWarnings("unchecked")
	private E empty() {
		return (E) EMPTY;
	}

	@Override
	public boolean hasNext() {
		return _val1 != empty() || _val2 != empty();
	}

	@Override
	public E next() {
		E result;
		if (_val1 == empty()) {
			if (_val2 == empty()) {
				throw new NoSuchElementException();
			} else {
				result = _val2;
				fillVal2();
			}
		} else {
			if (_val2 == empty()) {
				result = _val1;
				fillVal1();
			} else {
				int compare = _comparator.compare(_val1, _val2);
				if (compare <= 0) {
					result = _val1;
					fillVal1();
				} else {
					result = _val2;
					fillVal2();
				}
			}
		}
		return result;
	}

	private void fillVal1() {
		if (_it1.hasNext()) {
			_val1 = _it1.next();
		} else {
			_val1 = empty();
		}
	}

	private void fillVal2() {
		if (_it2.hasNext()) {
			_val2 = _it2.next();
		} else {
			_val2 = empty();
		}
	}

}
