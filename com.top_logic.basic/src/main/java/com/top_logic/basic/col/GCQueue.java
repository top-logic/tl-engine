/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Queue whose elements are immediately garbage collected, if all currently
 * alive {@link #iterator()}s have seen them.
 * 
 * <p>
 * An {@link #iterator()} of a {@link GCQueue} only sees those elements that are
 * {@link #add(Object) added} after the iterator was created.
 * </p>
 * 
 * <p>
 * A {@link GCQueue} is used to decouple producers from an potentially unlimited
 * and unknown number of concurrent consumers.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GCQueue<T> implements Iterable<T> {

	private GCQueue.Entry<T> newestEntry;

	/**
	 * Creates a new empty {@link GCQueue}.
	 */
	public GCQueue() {
		this.newestEntry = new GCQueue.Entry<>(null);
	}

	/**
	 * Adds a new value to this {@link GCQueue}.
	 * 
	 * <p>
	 * Unlike other collections, it is explicitly OK to add new values while
	 * iterators are active.
	 * </p>
	 * 
	 * @param value
	 *        The value that is broadcasted to all currently alive
	 *        {@link #iterator()}s.
	 */
	public synchronized void add(T value) {
		GCQueue.Entry<T> nextEntry = new GCQueue.Entry<>(value);
		newestEntry.setNext(nextEntry);
		newestEntry = nextEntry;
	}

	@Override
	public synchronized Iterator<T> iterator() {
		return new Iterator<>() {
			GCQueue.Entry<T> current = getNewestEntry();

			@Override
			public synchronized boolean hasNext() {
				return current.getNext() != null;
			}

			@Override
			public synchronized T next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				current = current.getNext();
				return current.value;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/*package protected*/ synchronized GCQueue.Entry<T> getNewestEntry() {
		return newestEntry;
	}

	private static class Entry<T> {

		/**
		 * The next entry whose value is consumed after this entry.
		 */
		private GCQueue.Entry<T> next;

		/*package protected*/ final T value;

		public Entry(T value) {
			this.value = value;
		}

		public synchronized void setNext(GCQueue.Entry<T> next) {
			this.next = next;
		}

		public synchronized GCQueue.Entry<T> getNext() {
			return next;
		}
	}

}