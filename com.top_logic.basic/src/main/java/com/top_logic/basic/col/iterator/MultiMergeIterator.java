/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.iterator;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;

/**
 * Iterator merging the given {@link Iterator} in comparator order.
 * 
 * @see IteratorUtil#mergeIterators(Comparator, java.util.List)
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MultiMergeIterator<E> implements Iterator<E> {

	private final PriorityQueue<ValueFromIterator<? extends E>> _values;

	/**
	 * Creates a {@link MultiMergeIterator}.
	 * 
	 * @param comparator
	 *        Comparator defining the order of the result iterator.
	 * @param iterators
	 *        {@link Iterator}s to merge. Each {@link Iterator} must return its values in
	 *        {@link Comparator} order.
	 */
	public MultiMergeIterator(Comparator<? super E> comparator, Iterator<? extends Iterator<? extends E>> iterators) {
		_values = new PriorityQueue<>(new ValueFromIteratorComparator<>(comparator));
		while (iterators.hasNext()) {
			Iterator<? extends E> iterator = iterators.next();
			if (iterator.hasNext()) {
				_values.add(new ValueFromIterator<>(iterator));
			}
		}
	}

	@Override
	public boolean hasNext() {
		return !_values.isEmpty();
	}

	@Override
	public E next() {
		if (_values.isEmpty()) {
			throw new NoSuchElementException();
		}
		ValueFromIterator<? extends E> nextValue = _values.poll();
		E result = nextValue._value;
		if (nextValue.update()) {
			_values.offer(nextValue);
		}
		return result;
	}

	private static class ValueFromIteratorComparator<E> implements Comparator<ValueFromIterator<? extends E>> {

		private Comparator<? super E> _valueComparator;

		public ValueFromIteratorComparator(Comparator<? super E> valueComparator) {
			_valueComparator = valueComparator;
		}

		@Override
		public int compare(ValueFromIterator<? extends E> o1, ValueFromIterator<? extends E> o2) {
			if (o1 == o2) {
				return 0;
			}
			return _valueComparator.compare(o1._value, o2._value);
		}

	}

	private static class ValueFromIterator<E> {

		Iterator<E> _iterator;

		E _value;

		private ValueFromIterator(Iterator<E> iterator) {
			_iterator = iterator;
			_value = _iterator.next();
		}

		boolean update() {
			if (_iterator.hasNext()) {
				_value = _iterator.next();
				return true;
			} else {
				return false;
			}
		}

	}

}
