/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Source of events.
 * 
 * <p>
 * In contrast to an {@link Iterator}, an {@link EventReader} must be
 * {@link #close() closed} after usage.
 * </p>
 * 
 * <p>
 * The type paramteter <code>E</code> represents the type of the events produced
 * by this reader.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface EventReader<E> extends Iterable<E> {

	/**
	 * Reads the next event from this reader.
	 * 
	 * @return The next read event, or <code>null</code>, if no more events are
	 *         available.
	 */
	E readEvent();

	/**
	 * Closes this {@link EventReader} and releases resources allocated during
	 * construction.
	 */
	void close();

	/**
	 * Adaptor that wrapps an {@link EventReader} into an {@link Iterator}.
	 * 
	 * <p>
	 * Note: The creator of such a wrapper is responsible for
	 * {@link EventReader#close() closing} the wrapped reader.
	 * </p>
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public class IteratorAdaptor<E> implements Iterator<E> {
		
		private E nextEvent;
		private final EventReader<E> reader;

		/**
		 * Wraps the given {@link EventReader} into an
		 * {@link EventReader.IteratorAdaptor}.
		 * 
		 * <p>
		 * Note: The caller is responsible for closing the given reader, after
		 * the constructed iterator is no longer in use.
		 * </p>
		 * 
		 * @param reader
		 *        The {@link EventReader} to wrap.
		 */
		public IteratorAdaptor(EventReader<E> reader) {
			this.reader = reader;
		}
		
		@Override
		public boolean hasNext() {
			if (this.nextEvent == null) {
				this.nextEvent = reader.readEvent();
			}
			
			return this.nextEvent != null;
		}
		
		@Override
		public E next() {
			E next;
			if (this.nextEvent == null) {
				next = this.reader.readEvent();
				if (next == null) {
					throw new NoSuchElementException("Iterator has no more elements.");
				}
			} else {
				next = nextEvent;
				nextEvent = null;
			}
			return next;
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException("Read-only iterator.");
		}
	
	}

}
