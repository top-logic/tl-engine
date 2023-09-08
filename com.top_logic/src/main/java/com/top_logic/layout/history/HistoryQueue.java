/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.history;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A {@link HistoryQueue} represents the client side list of history.
 * 
 * A {@link HistoryQueue} contains a collection of {@link IdentifiedEntry} which
 * are ordered. The size of the queue is the count of added elements to the
 * queue.
 * 
 * It contains an index whose range is [1, {@link HistoryQueue#size()}].
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
final class HistoryQueue {

	/**
	 * Number of entries which can be stored in this queue before the anterior
	 * entries are forgotten.
	 */
	private final int maxSize;

	private int replayIndex = 0;
	private IdentifiedEntry replayElement;

	/**
	 * the number of currently containing elements
	 */
	private int size = 0;

	/**
	 * First and last elements in the queue. They are either both
	 * <code>null</code> (iff the queue is empty), or both <code>notnull</code>
	 * (iff the queue is not empty).
	 */
	private IdentifiedEntry first, last;

	/**
	 * the index of {@link #currentElement} in the queue.
	 * 
	 * @see HistoryQueue#getIndex()
	 */
	private int index = 0;

	/**
	 * The element in the queue whose index is {@link #index}.
	 * 
	 * @see #current()
	 */
	private IdentifiedEntry currentElement;

	/**
	 * Counts the number of structural changes of the queue. It is used in
	 * {@link Iterator} returned by {@link #iterator(boolean)} to ensure that no
	 * concurrent modification happens.
	 */
	private int modCnt = 0;

	/**
	 * @see #getStackNumbers()
	 */
	private int stacks = 0;

	/**
	 * Element which will be offered to the client if the queue does not contain
	 * any elements but an element is requested. (as in {@link #current()} or
	 * {@link #elementToReplay()}).
	 */
	private IdentifiedEntry safetyElement;

	/**
	 * Creates a new {@link HistoryQueue}.
	 * 
	 * @param maxSize
	 *        the number of elements which can be added to the queue before the
	 *        oldest element will be removed.
	 * @param safetyElement
	 *        the element which will be returned if the queue is empty but it is
	 *        necessary to return some element as in {@link #current()} or
	 *        {@link #elementToReplay()}.
	 */
	public HistoryQueue(int maxSize, IdentifiedEntry safetyElement) {
		assert maxSize >= 0;
		this.maxSize = maxSize + 1;
		this.safetyElement = safetyElement;
		add(safetyElement);
	}

	void push() {
		stacks++;
		if (currentElement != null) {
			currentElement.setEndsStack(true);
		}
	}

	void pop() {
		assert noStackInFuture();

		if (currentElement != null) {
			if (!currentElement.endsStack()) {
				do {
					if (currentElement.endsStack()) {
						// elements ends stack iff some push was called after
						currentElement.setEndsStack(stacks - 1 > currentElement.getStackDepth());
						break;
					}
					decreaseCurrentIndex();
				} while (currentElement != null);

				removeFromIndex();
			} else {
				if (index < size) {
					removeFromIndex();
				} else {
					assert currentElement == last;
				}
				currentElement.setEndsStack(stacks - 1 > currentElement.getStackDepth());
			}
		}
		stacks--;
	}

	/**
	 * Returns the number of stacks which were opened. Will be increased by
	 * {@link #push()} and decreased by {@link #pop()}.
	 */
	int getStackNumbers() {
		return stacks;
	}

	/**
	 * Returns true iff there is no element after the {@link #current() indexed
	 * element} which ends a stack.
	 */
	private boolean noStackInFuture() {
		if (currentElement == null) {
			return true;
		}
		IdentifiedEntry elementToCheck = currentElement.next;
		while (elementToCheck != null) {
			if (elementToCheck.endsStack()) {
				return false;
			}
			elementToCheck = elementToCheck.next;
		}
		return true;
	}

	/**
	 * Returns the first element in this queue.
	 */
	IdentifiedEntry getFirst() {
		if (first == null) {
			assert last == null;
			return safetyElement;
		}
		return first;
	}

	/**
	 * Returns the last element in this queue.
	 */
	IdentifiedEntry getLast() {
		if (last == null) {
			assert first == null;
			throw new IllegalStateException("No entries in the HistoryQueue");
		}
		return last;
	}

	/**
	 * Returns the index of the {@link #current() current element} in the queue.
	 * Its range is [1, {@link #size()}], i.e. if this method returns 1, then
	 * {@link #current()} returns {@link #getFirst()}, if it returns
	 * {@link #size()} then {@link #current()} returns {@link #getLast()}.
	 */
	int getIndex() {
		return index;
	}

	/**
	 * Increases the index and updates the element given by {@link #current()},
	 * 
	 * @throws IndexOutOfBoundsException
	 *         iff the index already points to the end of the queue.
	 */
	void increaseCurrentIndex() {
		if (index == size) {
			throw new IndexOutOfBoundsException("Index already points to the end of the queue. Can not increase.");
		}
		if (index == 0) {
			currentElement = first;
		} else {
			currentElement = currentElement.next;
		}
		index++;
	}

	/**
	 * Decreases the index and updates the element given by {@link #current()},
	 * 
	 * @throws IndexOutOfBoundsException
	 *         iff the index already points to the begin of the queue.
	 * 
	 */
	void decreaseCurrentIndex() {
		if (index == 0) {
			throw new IndexOutOfBoundsException("Index already points to the begin of the queue. Can not decrease.");
		}
		index--;
		if (index == 0) {
			currentElement = null;
		} else {
			assert currentElement != null;
			currentElement = currentElement.previous;
		}
	}

	/**
	 * The returned element represents the state in the history on the client,
	 * i.e. if the current element is the last one in the queue then the history
	 * on the client is also at the end (no forward possible).
	 * 
	 * If it points to some element in the middle of the queue then the user can
	 * push back and the element returned by {@link #current()} will be undone.
	 * A forward will force the server to redo the element after the element
	 * returned by {@link #current()}.
	 * 
	 * If it points to the first element then it is logically not possible for
	 * the user to go back, i.e. maybe he can push the back button as the client
	 * has a larger history, but the server can not roll back any action, so the
	 * back is ignored.
	 */
	IdentifiedEntry current() {
		// assert 0 < index && index <= size :
		// "Index must not point before the start of the queue or after the last entry";
		if (currentElement == null) {
			return safetyElement;
		}
		return currentElement;
	}

	/**
	 * Removes all elements in the queue beginning with the element after
	 * {@link #current()}.
	 */
	private void removeFromIndex() {
		if (index >= size) {
			return;
		} else {
			if (index <= 0) {
				clear();
			} else {
				modCnt++;

				assert currentElement != null : "As index is larger than '0' there must be an indexed element";
				currentElement.next = null;
				// current element is now also the last one
				last = currentElement;
				size = index;
			}
		}
	}

	/**
	 * Removes all entries in the queue.
	 */
	void clear() {
		modCnt++;
		last = null;
		first = null;
		size = 0;
		currentElement = null;
		index = 0;
	}

	/**
	 * The replay index is used in case some history must be ported from the
	 * server to the client, e.g. if the user pushed F5 and the client removed
	 * the history.
	 * 
	 * The returned index is the index of the element which will be replayed as
	 * next.
	 * 
	 */
	int getReplayIndex() {
		return replayIndex;
	}

	/**
	 * Increases the {@link #getReplayIndex() replay index} and updates the
	 * {@link #elementToReplay() next element to replay}.
	 * 
	 * @throws IndexOutOfBoundsException
	 *         iff the element to replay already points to the end of the queue.
	 */
	void increaseReplayIndex() {
		if (replayIndex == size) {
			throw new IndexOutOfBoundsException("ReplayIndex must not be larger than size(). CurrentReplayIndex:" + replayIndex + ", size:" + size);
		}
		if (replayIndex == 0) {
			replayElement = first;
		} else {
			replayElement = replayElement.next;
		}
		replayIndex++;
	}

	/**
	 * Decreases the {@link #getReplayIndex() replay index} and updates the
	 * {@link #elementToReplay() next element to replay}.
	 * 
	 * @throws IndexOutOfBoundsException
	 *         iff the element to replay already points to the start of the
	 *         queue.
	 * 
	 */
	void decreaseReplayIndex() {
		if (replayIndex == 0) {
			throw new IndexOutOfBoundsException("ReplayIndex must not be smaller than 0. CurrentReplayIndex:" + replayIndex);
		}
		replayIndex--;
		if (replayIndex == 0) {
			replayElement = safetyElement;
		} else {
			assert replayElement != safetyElement;
			replayElement = replayElement.previous;
		}
	}

	/**
	 * Returns the element which must be brought to the client as next.
	 */
	IdentifiedEntry elementToReplay() {
		return replayElement;
	}

	/**
	 * Installs the replay index, i.e. the index given as argument will be
	 * returned by {@link #getReplayIndex()}. Moreover it updates the
	 * {@link #elementToReplay()}.
	 * 
	 * @param newIndex
	 *        the new replay index. If the new index is
	 *        <code>&lt;=0 || &gt;size</code> the {@link #getReplayIndex()
	 *        replay index} is set to 0 and the {@link #elementToReplay()} to
	 *        the {@link #safetyElement}.
	 */
	void setReplayIndex(int newIndex) {
		if (newIndex <= 0 || newIndex > size) {
			replayElement = safetyElement;
			replayIndex = 0;
			return;
		}
		replayIndex = newIndex;
		switch (replayIndex) {
			case 1:
				replayElement = first;
				break;
			default:
				if (replayIndex <= size / 2) {
					replayElement = first;
					for (int _index = 1; _index < replayIndex; _index++) {
						replayElement = replayElement.next;
					}
				} else {
					replayElement = last;
					for (int _index = size; _index > replayIndex; _index--) {
						replayElement = replayElement.previous;
					}
				}
				break;
		}
	}

	/**
	 * Adds the given element to the end of the list. If the {@link #getIndex()
	 * index} does currently not point to the end of the queue, all elements
	 * after the {@link #current() indexed element} are removed.
	 */
	boolean add(IdentifiedEntry o) {

		// remember at which stack the IdentifiedEntry was added
		o.setStackDepth(stacks);

		removeFromIndex();

		modCnt++;
		if (last == null) {
			last = o;
			first = last;
		} else {
			IdentifiedEntry _last = last;
			o.previous = _last;
			_last.next = o;
			last = o;
		}
		size++;

		if (size > maxSize) {
			removeFirst();// decreases size and index
		}

		// if index pointed after the last element, it was equal to size()
		// *before* the adding.
		if (index == size - 1) {
			increaseCurrentIndex();
		}
		// as linked list does
		return true;
	}

	/**
	 * Returns the number of elements in this queue
	 */
	int size() {
		return size;
	}

	/**
	 * Removes the first element in the list
	 * 
	 * @return whether the list has changed
	 */
	boolean removeFirst() {
		if (first == null) {
			// empty list
			return false;
		}
		modCnt++;
		IdentifiedEntry second = first.next;
		if (second == null) {
			// only one element in list
			first = last = null;
		} else {
			second.previous = null;
			first = second;
		}
		decreaseSize();
		return true;
	}

	private void decreaseSize() {
		size--;
		if (index != 0) {
			// to ensure that get on the index returnes the currentElement the
			// index must also be decreased.
			assert currentElement != null;
			index--;
		}
	}

	/**
	 * Returns an iterator over all added elements
	 * 
	 * @param reverse
	 *        whether the iterator shall start with the end or the begin of the
	 *        list.
	 */
	Iterator<IdentifiedEntry> iterator(final boolean reverse) {
		if (last == null) {
			return EmptyIterator.getInstance();
		}

		return new Iterator<>() {

			int modCnt = HistoryQueue.this.modCnt;
			IdentifiedEntry current = reverse ? last : first;

			@Override
			public boolean hasNext() {
				return current != null;
			}

			@Override
			public IdentifiedEntry next() {
				if (this.modCnt != HistoryQueue.this.modCnt) {
					throw new ConcurrentModificationException("Queue was changed concurrently.");
				}
				if (current == null) {
					throw new NoSuchElementException();
				}
				IdentifiedEntry result = current;
				current = reverse ? current.previous : current.next;
				return result;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * The class {@link EmptyIterator} is an iterator which contains no
	 * elements.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	private static final class EmptyIterator implements Iterator<Object> {

		@SuppressWarnings("unchecked")
		private static Iterator INSTANCE = new EmptyIterator();

		@SuppressWarnings("unchecked")
		static <E> Iterator<E> getInstance() {
			return INSTANCE;
		}

		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public Object next() {
			throw new NoSuchElementException();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public boolean isEmpty() {
		return size == 0;
	}

}
