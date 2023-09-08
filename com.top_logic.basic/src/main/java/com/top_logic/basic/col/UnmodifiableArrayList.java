/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.StringServices;

/**
 * List that based on a given object array and is completely unmodifiable, i.e. in contrast to
 * {@link Arrays#asList(Object...)}, it is not possible to change the values of the the list via the
 * list API.
 * 
 * Changes on the base array are reflected to the list.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class UnmodifiableArrayList<E> extends AbstractCollection<E> implements List<E>, RandomAccess, Serializable {

	/**
	 * Creates an unmodifiable {@link RandomAccess} list based on the given storage.
	 * 
	 * @param storage
	 *        the storage used to get the data of the list, i.e. if it is changed, the list gets new
	 *        values.
	 */
	public static <E> List<E> newUnmodifiableList(E[] storage) {
		if (storage == null) {
			throw new NullPointerException("'storage' must not be 'null'.");
		}
		if (storage.length == 0) {
			return Collections.emptyList();
		}
		return new UnmodifiableArrayList<>(storage);
	}

	/** ID for serialisation. */
	private static final long serialVersionUID = 666L;

	final E[] _storage;

	UnmodifiableArrayList(E[] storage) {
		assert storage != null && storage.length > 0;
		_storage = storage;
	}

	@Override
	public E get(int index) {
		return _storage[index];
	}

	@Override
	public int size() {
		return _storage.length;
	}

	@Override
	public boolean isEmpty() {
		// factory method checks that storage is not empty
		return false;
	}

	@Override
	public boolean contains(Object o) {
		return indexOf(o) != -1;
	}

	@Override
	public Object[] toArray() {
		// can not use clone method as that also uses type information about the array.
		int size = size();
		Object[] result = new Object[size];
		System.arraycopy(this._storage, 0, result, 0, size);
		return result;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		int size = size();
		if (a.length < size) {
			@SuppressWarnings("unchecked")
			Class<? extends T[]> newType = (Class<? extends T[]>) a.getClass();
			return Arrays.copyOf(this._storage, size, newType);
		}
		System.arraycopy(this._storage, 0, a, 0, size);
		if (a.length > size) {
			// must be done due to javadoc comment.
			a[size] = null;
		}
		return a;
	}

	@Override
	public int indexOf(Object o) {
		if (o == null) {
			for (int i = 0; i < size(); i++)
				if (_storage[i] == null)
					return i;
		} else {
			for (int i = 0; i < size(); i++)
				if (o.equals(_storage[i]))
					return i;
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object o) {
		if (o == null) {
			for (int i = size() - 1; i > 0; i--)
				if (_storage[i] == null)
					return i;
		} else {
			for (int i = size() - 1; i > 0; i--)
				if (o.equals(_storage[i]))
					return i;
		}
		return -1;
	}

	@Override
	public ListIterator<E> listIterator() {
		return listIterator(0);
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return ArrayUtil.listIterator(index, _storage);
	}

	@Override
	public List<E> subList(final int fromIndex, final int toIndex) {
		if (fromIndex < 0)
			throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
		if (toIndex > size())
			throw new IndexOutOfBoundsException("toIndex = " + toIndex);
		if (fromIndex > toIndex)
			throw new IndexOutOfBoundsException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
		if (fromIndex == toIndex)
			return Collections.emptyList();
		if (fromIndex == 0 && toIndex == size()) {
			return this;
		}
		return new AbstractList<>() {

			@Override
			public E get(int index) {
				int storageIndex = index + fromIndex;
				if (storageIndex < fromIndex || storageIndex >= toIndex) {
					throw new IndexOutOfBoundsException();
				}
				return _storage[storageIndex];
			}

			@Override
			public int size() {
				return toIndex - fromIndex;
			}

		};
	}

	@Override
	public Iterator<E> iterator() {
		return ArrayUtil.iterator(_storage);
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		throw unmodifiable();
	}

	@Override
	public E set(int index, E element) {
		throw unmodifiable();
	}

	@Override
	public void add(int index, E element) {
		throw unmodifiable();
	}

	@Override
	public E remove(int index) {
		throw unmodifiable();
	}

	private static UnsupportedOperationException unmodifiable() {
		throw new UnsupportedOperationException("This list implementation is not modifiable");
	}

	@Override
	public int hashCode() {
		int hashCode = 1;
		for (Object obj : _storage) {
			hashCode = 31 * hashCode + (obj == null ? 0 : obj.hashCode());
		}
		return hashCode;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof List)) {
			return false;
		}
		List<?> other = (List<?>) o;
		int size = size();
		if (other.size() != size) {
			return false;
		}
		ListIterator<?> it = other.listIterator();
		int i = 0;
		while (i < size && it.hasNext()) {
			E o1 = get(i++);
			Object o2 = it.next();
			if (!StringServices.equals(o1, o2)) {
				return false;
			}
		}
		return i == size && !it.hasNext();
	}
}
