/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.core.xml;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.w3c.dom.Node;

/**
 * Base class for implementing DOM iteratbles.
 * 
 * @see ChildIterator The corresponding {@link Iterator} implementation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class DOMIterable<T extends Node> implements Iterable<T> {

	/*package protected*/ final Node _parent;

	/*package protected*/ final boolean _reverse;

	/**
	 * Creates a {@link DOMIterable}.
	 * 
	 * @param parent
	 *        The node to take children from.
	 * @param reverse
	 *        Whether to report in reverse document order.
	 */
	protected DOMIterable(Node parent, boolean reverse) {
		_parent = parent;
		_reverse = reverse;
	}

	/**
	 * Base class for {@link Iterator}s over children of DOM {@link Node}s.
	 * 
	 * <p>
	 * Note: The iterator must be implemented as a non-static inner class, because it must be able
	 * to access all information passed to the outer iterable within its top-level constructor.
	 * </p>
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public abstract class ChildIterator<N extends Node> implements Iterator<N> {

		private N _next;

		private N _last;

		/**
		 * Creates a {@link DOMIterable.ChildIterator} over all children of the given node in the
		 * given order.
		 */
		public ChildIterator() {
			_next = match(findFirst(_parent));
		}

		@Override
		public boolean hasNext() {
			return _next != null;
		}

		/**
		 * Find the next matching sibling of the given node.
		 * 
		 * <p>
		 * If the given node itself matches the query, it must be returned.
		 * </p>
		 */
		protected N match(Node node) {
			while (true) {
				if (node == null) {
					return null;
				} else if (matches(node)) {
					return cast(node);
				} else {
					node = findNext(node);
				}
			}
		}

		/**
		 * Decides whether the given {@link Node} maches the query.
		 */
		protected abstract boolean matches(Node node);

		/**
		 * Casts the given matching node to the concrete result type.
		 */
		protected abstract N cast(Node node);

		@Override
		public N next() {
			if (_next == null) {
				throw new NoSuchElementException();
			}
			_last = _next;
			_next = match(findNext(_next));
			return _last;
		}

		@Override
		public void remove() {
			if (_last == null) {
				throw new IllegalStateException("Method next() not called.");
			}
			_last.getParentNode().removeChild(_last);
			_last = null;
		}

		private Node findFirst(Node node) {
			if (_reverse) {
				return node.getLastChild();
			} else {
				return node.getFirstChild();
			}
		}

		private Node findNext(Node node) {
			if (_reverse) {
				return node.getPreviousSibling();
			} else {
				return node.getNextSibling();
			}
		}

	}

}
