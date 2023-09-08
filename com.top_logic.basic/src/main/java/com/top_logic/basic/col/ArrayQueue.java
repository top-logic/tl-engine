/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * Implementation of a FIFO-{@link Queue} based on an array.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ArrayQueue<E> extends AbstractQueue<E> {

	/**
	 * Storage is considered as circle, i.e. the first element in the array may
	 * be a sibling of the last element in the array
	 */
	private E[] storage;

	/** index of the first element in the {@link Queue} */
	private int headIndex = 0;

	/** number of elements in the {@link Queue} */
	int size = 0;

	/** number of structure changing access to this queue. Used to determine modification during iterating o*/
	int modCount;

	/** 
	 * Creates a new {@link ArrayQueue} with the given size.
	 * 
	 * @param initialSize the initial size of the storage
	 */
	public ArrayQueue(int initialSize) {
		this.storage = ArrayQueue.<E>newStorage(initialSize);
	}

	/** 
	 * Creates a new {@link ArrayQueue} with the initial size 16.
	 */
	public ArrayQueue() {
		this(16);
	}
	
	@SuppressWarnings("unchecked")
	private static <F> F[] newStorage(int initialSize) {
		return (F[]) new Object[initialSize];
	}
	
	private void ensureCapacity() {
		if (size == storage.length) {
			int newCapacity = (storage.length * 3) / 2 + 1;
			storage = copyStorage(newCapacity);
			headIndex = 0;
		}
	}

	private E[] copyStorage(int newCapacity) {
		E[] newStorage = ArrayQueue.<E>newStorage(newCapacity);
		final int x = storage.length - headIndex;
		if (x > size) {
			System.arraycopy(storage, headIndex, newStorage, 0, size);
		} else {
			System.arraycopy(storage, headIndex, newStorage, 0, x);
			System.arraycopy(storage, 0, newStorage, x, headIndex);
		}
		return newStorage;
	}

	private int getStorageIndex(int index) {
		return (headIndex + index) % storage.length;
	}

	private void computeNextIndex() {
		if (headIndex == storage.length - 1) {
			headIndex = 0;
		} else {
			headIndex++;
		}
	}

	private boolean checkEmpty() {
		for (Object o : storage) {
			if (o != null) {
				return false;
			}
		}
		return true;
	}

	@Override
	public E poll() {
		if (size > 0) {
			E o = storage[headIndex];
			storage[headIndex] = null; // For GC
			computeNextIndex();
			size--;
			modCount++;
			return o;
		}
		assert checkEmpty() : "Storage not empty: " + Arrays.toString(storage);
		return null;
	}

	@Override
	public boolean offer(E o) {
		ensureCapacity();
		storage[getStorageIndex(size)] = o;
		size++;
		modCount++;
		return true;
	}

	@Override
	public E peek() {
		if (size > 0) {
			return storage[headIndex];
		}
		return null;
	}

	E getElementAt(int i) {
		if (i >= size) {
			throw new IndexOutOfBoundsException(); 
		}
		return storage[getStorageIndex(i)];
	}

	void removeElementAt(int i) {
		int storageIndexToRemove = getStorageIndex(i);
		int lastElementIndex = getStorageIndex(size - 1);
		int elementsAfterIndex = storage.length - headIndex;
		int elementsBeforeIndex = size - elementsAfterIndex;
		if (storageIndexToRemove >= headIndex) {
			if (lastElementIndex >= storageIndexToRemove) {
				System.arraycopy(storage, storageIndexToRemove + 1, storage, storageIndexToRemove, lastElementIndex
						- storageIndexToRemove);
			} else {
				System.arraycopy(storage, storageIndexToRemove + 1, storage, storageIndexToRemove, storage.length - 1
						- storageIndexToRemove);
				storage[storage.length - 1] = storage[0];
				System.arraycopy(storage, 1, storage, 0, elementsBeforeIndex - 1);
			}
		} else {
			System.arraycopy(storage, storageIndexToRemove, storage, storageIndexToRemove + 1, elementsBeforeIndex
					- storageIndexToRemove);
		}
		storage[lastElementIndex] = null; // For GC
		size--;

		modCount++;
	}

	@Override
	public Iterator<E> iterator() {
		return new Iterator<>() {

			int numberModifications = ArrayQueue.this.modCount;

			int i = 0;

			@Override
			public boolean hasNext() {
				checkConcurrentModification();
				return i < size;
			}

			@Override
			public E next() {
				checkConcurrentModification();
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				return getElementAt(i++);
			}

			@Override
			public void remove() {
				checkConcurrentModification();
				removeElementAt(i);
				numberModifications = ArrayQueue.this.modCount;
			}

			private void checkConcurrentModification() {
				if (ArrayQueue.this.modCount != numberModifications) {
					throw new ConcurrentModificationException();
				}
			}
		};
	}

	@Override
	public int size() {
		return size;
	}

}
