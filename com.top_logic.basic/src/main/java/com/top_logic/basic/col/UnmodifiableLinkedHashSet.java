/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.function.Predicate;

import com.top_logic.basic.col.iterator.UnmodifiableIterator;

/**
 * An unmodifiable {@link LinkedHashSet}.
 * <p>
 * Throws an {@link UnsupportedOperationException} when a method is called that could change this
 * {@link LinkedHashSet}. For example, even if it is already empty and {@link #clear()} is called,
 * that call will fail.
 * </p>
 * <p>
 * Unlike the "unmodifiableXxx" methods in {@link Collections}, this class creates a copy of the
 * given Collection and not just wraps it. That way, this class is a subtype of
 * {@link LinkedHashSet} and therefore assignment compatible. As there is no interface for
 * "LinkedSet", this is the only way to make it possible to use an {@link UnmodifiableLinkedHashSet}
 * wherever a normal {@link LinkedHashSet} is expected.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class UnmodifiableLinkedHashSet<E> extends LinkedHashSet<E> {

	private static final UnmodifiableLinkedHashSet<?> EMPTY = new UnmodifiableLinkedHashSet<>();

	/** The empty {@link UnmodifiableLinkedHashSet}. */
	@SuppressWarnings("unchecked")
	public static <E> UnmodifiableLinkedHashSet<E> empty() {
		return (UnmodifiableLinkedHashSet<E>) EMPTY;
	}

	private boolean initialized = false;

	/**
	 * Creates an {@link UnmodifiableLinkedHashSet}.
	 * 
	 * @see LinkedHashSet#LinkedHashSet()
	 */
	private UnmodifiableLinkedHashSet() {
		super(0, 1);
		initialized = true;
	}

	/**
	 * Creates an {@link UnmodifiableLinkedHashSet}.
	 * 
	 * @see LinkedHashSet#LinkedHashSet(Collection)
	 */
	public UnmodifiableLinkedHashSet(Collection<? extends E> collection) {
		/* The factor 1.5 was chosen to reduce the number of conflicts. */
		super((int) (collection.size() * 1.5f), 1);
		addAll(collection);
		initialized = true;
	}

	@Override
	public boolean removeIf(Predicate<? super E> filter) {
		throw new UnsupportedOperationException("This is an unmodifiable collection.");
	}

	@Override
	public Iterator<E> iterator() {
		return new UnmodifiableIterator<>(super.iterator());
	}

	@Override
	public boolean add(E e) {
		if (initialized) {
			throw new UnsupportedOperationException("This is an unmodifiable collection.");
		}
		return super.add(e);
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("This is an unmodifiable collection.");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("This is an unmodifiable collection.");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException("This is an unmodifiable collection.");
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		if (initialized) {
			throw new UnsupportedOperationException("This is an unmodifiable collection.");
		}
		return super.addAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("This is an unmodifiable collection.");
	}

}
