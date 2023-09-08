/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * {@link Set} that bases on an {@link IdentityHashMap}.
 * 
 * <p>
 * <b>This class is <i>not</i> a general-purpose {@link Set} implementation! While this class
 * implements the {@link Set} interface, it intentionally violates {@link Set}'s general contract,
 * which mandates the use of the <tt>equals</tt> method when comparing objects. This class is
 * designed for use only in the rare cases wherein reference-equality semantics are required.</b>
 * </p>
 * 
 * @see HashSet inspired by implementation in HashSet.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class IdentityHashSet<E> extends AbstractSet<E> implements Cloneable {

	private transient IdentityHashMap<E, Object> _map;

	// Dummy value to associate with an Object in the backing Map
	private static final Object PRESENT = new Object();

	/**
	 * Constructs a new, empty set; the backing {@link IdentityHashMap} instance has default
	 * expected maximum size (21).
	 */
	public IdentityHashSet() {
		_map = new IdentityHashMap<>();
	}

	/**
	 * Constructs a new set containing the elements in the specified collection. The
	 * {@link IdentityHashMap} is created with expected size sufficient to contain the elements in
	 * the specified collection.
	 * 
	 * @param c
	 *        the collection whose elements are to be placed into this set
	 * 
	 * @throws NullPointerException
	 *         if the specified collection is <code>null</code>
	 */
	public IdentityHashSet(Collection<? extends E> c) {
		_map = new IdentityHashMap<>((int) ((1 + c.size()) * 1.1));
		addAll(c);
	}

	/**
	 * Constructs a new, empty set; the backing {@link IdentityHashMap} instance has the specified
	 * initial max size.
	 * 
	 * @param expectedMaxSize
	 *        the initial capacity of the hash table
	 * 
	 * @throws IllegalArgumentException
	 *         if the initial capacity is less than zero
	 */
	public IdentityHashSet(int expectedMaxSize) {
		_map = new IdentityHashMap<>(expectedMaxSize);
	}

	/**
	 * Returns an iterator over the elements in this set. The elements are returned in no particular
	 * order.
	 * 
	 * @return an Iterator over the elements in this set
	 * 
	 * @see ConcurrentModificationException
	 */
	@Override
	public Iterator<E> iterator() {
		return _map.keySet().iterator();
	}

	/**
	 * Returns the number of elements in this set (its cardinality).
	 * 
	 * @return the number of elements in this set (its cardinality)
	 */
	@Override
	public int size() {
		return _map.size();
	}

	/**
	 * Returns <tt>true</tt> if this set contains no elements.
	 * 
	 * @return <tt>true</tt> if this set contains no elements
	 */
	@Override
	public boolean isEmpty() {
		return _map.isEmpty();
	}

	/**
	 * Returns <tt>true</tt> if this set contains the specified element. More formally, returns
	 * <tt>true</tt> if and only if this set contains an element <tt>e</tt> such that
	 * <tt>(o == e)</tt>.
	 * 
	 * @param o
	 *        element whose presence in this set is to be tested
	 * 
	 * @return <tt>true</tt> if this set contains the specified element
	 */
	@Override
	public boolean contains(Object o) {
		return _map.containsKey(o);
	}

	/**
	 * Adds the specified element to this set if it is not already present. More formally, adds the
	 * specified element <tt>e</tt> to this set if this set contains no element <tt>e2</tt> such
	 * that <tt>(e == e2)</tt>. If this set already contains the element, the call leaves the set
	 * unchanged and returns <tt>false</tt>.
	 * 
	 * @param e
	 *        element to be added to this set
	 * 
	 * @return <tt>true</tt> if this set did not already contain the specified element
	 */
	@Override
	public boolean add(E e) {
		return _map.put(e, PRESENT) == null;
	}

	/**
	 * Removes the specified element from this set if it is present. More formally, removes an
	 * element <tt>e</tt> such that <tt>(o == e)</tt>, if this set contains such an element. Returns
	 * <tt>true</tt> if this set contained the element (or equivalently, if this set changed as a
	 * result of the call). (This set will not contain the element once the call returns.)
	 * 
	 * @param o
	 *        object to be removed from this set, if present
	 * 
	 * @return <tt>true</tt> if the set contained the specified element
	 */
	@Override
	public boolean remove(Object o) {
		return _map.remove(o) == PRESENT;
	}

	/**
	 * Removes all of the elements from this set. The set will be empty after this call returns.
	 */
	@Override
	public void clear() {
		_map.clear();
	}

	/**
	 * Returns a shallow copy of this {@link IdentityHashSet} instance: the elements themselves are
	 * not cloned.
	 * 
	 * @return a shallow copy of this set
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object clone() {
		try {
			IdentityHashSet<E> newSet = (IdentityHashSet<E>) super.clone();
			newSet._map = (IdentityHashMap<E, Object>) _map.clone();
			return newSet;
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}

}
