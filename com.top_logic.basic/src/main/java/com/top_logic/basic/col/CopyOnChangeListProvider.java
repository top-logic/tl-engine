/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.RandomAccess;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.CollectionUtil;

/**
 * The class {@link CopyOnChangeListProvider} provides access to a internal list which is guaranteed
 * not to be changed.
 * 
 * <p>
 * The implementation based on the idea of {@link CopyOnWriteArrayList} which allows to access
 * multi-threaded access to a list by copying the underlying array. In contrast to that
 * implementation the whole list is copied when it is changed.
 * </p>
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CopyOnChangeListProvider<E> implements Provider<List<E>> {

	private transient final Lock _lock = new ReentrantLock();

	private volatile List<Object> _impl = Collections.emptyList();

	/**
	 * @see CopyOnWriteArrayList#addIfAbsent(Object)
	 */
	public boolean addIfAbsent(E e) {
		final Lock lock = this._lock;
		lock.lock();
		try {
			// Copy while checking if already present.
			// This wins in the most common case where it is not present
			List<Object> currentList = _impl;
			int len = currentList.size();
			Object[] newElements = new Object[len + 1];
			if (e == null) {
				for (int i = 0; i < len; ++i) {
					if (null == currentList.get(i)) {
						return false; // exit, throwing away copy
					} else {
						newElements[i] = currentList.get(i);
					}
				}
			} else {
				for (int i = 0; i < len; ++i) {
					if (e.equals(currentList.get(i))) {
						return false; // exit, throwing away copy
					} else {
						newElements[i] = currentList.get(i);
					}
				}
			}
			newElements[len] = e;
			setImplementation(newElements);
			return true;
		} finally {
			lock.unlock();
		}

	}

	/**
	 * @see List#add(Object)
	 */
	public boolean add(E e) {
		final Lock lock = _lock;
		lock.lock();
		try {
			List<Object> currentList = _impl;
			int size = currentList.size();
			internalAdd(currentList, size, size, e);
			return true;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * @see List#remove(Object)
	 */
	public boolean remove(Object o) {
		final Lock lock = _lock;
		lock.lock();
		try {
			List<Object> oldImpl = _impl;
			int i = 0;
			int size = oldImpl.size();
			if (o == null) {
				for (; i < size; i++) {
					if (null == oldImpl.get(i)) {
						internalRemove(oldImpl, size, i);
						return true;
					}
				}
			} else {
				for (; i < size; i++) {
					if (o.equals(oldImpl.get(i))) {
						internalRemove(oldImpl, size, i);
						return true;
					}
				}
			}
			return false;
		} finally {
			lock.unlock();
		}
	}

	private Object internalRemove(List<Object> oldImpl, int size, int index) {
		Object removedObject = oldImpl.get(index);
		Object[] storage = new Object[size - 1];
		if (index > 0) {
			for (int i = 0; i < index; i++) {
				storage[i] = oldImpl.get(i);
			}
		}
		if (index < size - 1) {
			for (int i = index + 1; i < size; i++) {
				storage[i - 1] = oldImpl.get(i);
			}
		}
		setImplementation(storage);
		return removedObject;
	}

	/**
	 * @see List#addAll(Collection)
	 */
	public boolean addAll(Collection<? extends E> c) {
		if (c == null) {
			throw new NullPointerException();
		}
		int size = c.size();
		if (size == 0) {
			return false;
		}
		final Lock lock = _lock;
		lock.lock();
		try {
			List<Object> oldStorage = _impl;
			int oldLength = oldStorage.size();
			internalAddAll(oldStorage, oldLength, oldLength, c, size);
			return true;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * @see List#addAll(int, Collection)
	 */
	public boolean addAll(int index, Collection<? extends E> c) {
		if (c == null) {
			throw new NullPointerException();
		}
		int size = c.size();
		if (size == 0) {
			return false;
		}
		final Lock lock = _lock;
		lock.lock();
		try {
			List<Object> oldStorage = _impl;
			int oldSize = oldStorage.size();
			if (index > oldSize) {
				throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
			}
			internalAddAll(oldStorage, oldSize, index, c, size);
			return true;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * @see List#removeAll(Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		if (c == null) {
			throw new NullPointerException();
		}
		int size = c.size();
		if (size == 0) {
			return false;
		}
		final Lock lock = _lock;
		lock.lock();
		try {
			List<Object> oldStorage = _impl;
			int oldSize = oldStorage.size();
			if (oldSize == 0) {
				return false;
			}
			Object[] tmp = new Object[oldSize];
			int newSize = 0;
			for (Object o : oldStorage) {
				if (!c.contains(o)) {
					tmp[newSize++] = o;
				}
			}
			if (newSize == oldSize) {
				return false;
			}
			setImplementation(Arrays.copyOf(tmp, newSize));
			return true;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * @see List#retainAll(Collection)
	 */
	public boolean retainAll(Collection<?> c) {
		if (c == null) {
			throw new NullPointerException();
		}
		int size = c.size();
		if (size == 0) {
			return false;
		}
		final Lock lock = _lock;
		lock.lock();
		try {
			List<Object> oldStorage = _impl;
			int oldSize = oldStorage.size();
			if (oldSize == 0) {
				return false;
			}
			Object[] tmp = new Object[oldSize];
			int newSize = 0;
			for (Object o : oldStorage) {
				if (c.contains(o)) {
					tmp[newSize++] = o;
				}
			}
			if (newSize == oldSize) {
				return false;
			}
			setImplementation(Arrays.copyOf(tmp, newSize));
			return true;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * @see List#clear()
	 */
	public void clear() {
		final Lock lock = _lock;
		lock.lock();
		try {
			setImplementation(ArrayUtil.EMPTY_ARRAY);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * @see List#set(int, Object)
	 */
	public E set(int index, E element) {
		if (index < 0) {
			throw new IndexOutOfBoundsException("Index: " + index);
		}
		final Lock lock = _lock;
		lock.lock();
		try {
			Object[] storage = _impl.toArray();
			int size = storage.length;
			if (index >= size) {
				throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
			}
			@SuppressWarnings("unchecked")
			E formerElement = (E) storage[index];
			storage[index] = element;
			setImplementation(storage);
			return formerElement;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * @see List#add(int, Object)
	 */
	public void add(int index, E element) {
		if (index < 0) {
			throw new IndexOutOfBoundsException("Index: " + index);
		}
		final Lock lock = _lock;
		lock.lock();
		try {
			List<Object> currentList = _impl;
			int size = currentList.size();
			if (index > size) {
				throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
			}
			internalAdd(currentList, size, index, element);
		} finally {
			lock.unlock();
		}
	}

	private void internalAdd(List<Object> oldStorage, int oldLength, int index, E element) {
		Object[] storage = new Object[oldLength + 1];
		if (index > 0) {
			for (int i = 0; i < index; i++) {
				storage[i] = oldStorage.get(i);
			}
		}
		storage[index] = element;
		if (index < oldLength) {
			for (int i = index; i < oldLength; i++) {
				storage[i + 1] = oldStorage.get(i);
			}
		}
		setImplementation(storage);
	}

	private void internalAddAll(List<Object> oldStorage, int oldLength, int index, Collection<? extends E> c, int cSize) {
		Object[] storage = new Object[oldLength + cSize];
		if (index > 0) {
			for (int i = 0; i < index; i++) {
				storage[i] = oldStorage.get(i);
			}
		}
		int insertIndex = index;
		for (Object o : c) {
			storage[insertIndex++] = o;
		}

		if (index < oldLength) {
			assert insertIndex == index + cSize;
			for (int i = index; i < oldLength; i++) {
				storage[insertIndex++] = oldStorage.get(i);
			}
		}
		setImplementation(storage);
	}

	/**
	 * @see List#remove(int)
	 */
	public E remove(int index) {
		if (index < 0) {
			throw new IndexOutOfBoundsException("Index: " + index);
		}
		final Lock lock = _lock;
		lock.lock();
		try {
			List<Object> oldList = _impl;
			int size = oldList.size();
			if (index >= size) {
				throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
			}
			@SuppressWarnings("unchecked")
			E removed = (E) internalRemove(oldList, size, index);
			return removed;
		} finally {
			lock.unlock();
		}
	}

	private void setImplementation(Object[] storage) {
		_impl = CollectionUtil.unmodifiableList(storage);
	}

	/**
	 * Returns the unmodifiable {@link RandomAccess} version of the stored list. It is guaranteed
	 * that the returned list is not modified.
	 * 
	 * @see com.top_logic.basic.col.Provider#get()
	 */
	@Override
	public List<E> get() {
		@SuppressWarnings("unchecked")
		List<E> typeSafe = (List<E>) _impl;
		return typeSafe;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ':' + _impl.toString();
	}

}
