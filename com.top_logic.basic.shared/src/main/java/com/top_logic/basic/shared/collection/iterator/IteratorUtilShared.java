/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.shared.collection.iterator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.Stream;

import com.top_logic.basic.shared.collection.CollectionUtilShared;

/**
 * Methods and classes useful when working with {@link Iterator}s. They are collected here even if
 * they have their own class and file to easily find them.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class IteratorUtilShared {

	/** Instance of an empty iterator. */
	public static final Iterator<?> EMPTY_ITERATOR = Collections.EMPTY_LIST.iterator();

	/**
	 * Returns a typed empty iterator.
	 */
	public static final <T> Iterator<T> emptyIterator() {
		return Collections.<T> emptyList().iterator();
	}

	/**
	 * Returns an {@link Iterable} wrapper for the given {@link Stream}. Allows to use a
	 * for-each-loop to traverse the {@link Stream}.
	 * <p>
	 * <b>The returned {@link Iterable} cannot be reused. In case the {@link Iterable#iterator()}
	 * method is called twice, an {@link IllegalStateException} is thrown</b>.
	 * </p>
	 * 
	 * @throws NullPointerException
	 *         if the given iterator is <code>null</code>.
	 */
	public static <T> Iterable<T> toIterable(Stream<T> stream) {
		return toIterable(stream.iterator());
	}

	/**
	 * Returns a {@link Iterable} wrapper for the given {@link Iterator}. Allows to use a
	 * for-each-loop to traverse the iterator.
	 * 
	 * <b>The returned {@link Iterable} cannot be reused as it creates no new Iterator. In case the
	 * {@link Iterable#iterator()} method is called twice, an {@link IllegalStateException} is
	 * thrown</b>.
	 * 
	 * @throws NullPointerException
	 *         if the given iterator is <code>null</code>.
	 */
	public static <T> Iterable<T> toIterable(final Iterator<T> iterator) {
		if (iterator == null) {
			// Fail early (makes failure analysis easier)
			throw new NullPointerException("The given iterator is null!");
		}
		return new Iterable<T>() {

			Iterator<T> data = iterator;

			@Override
			public Iterator<T> iterator() {
				if (data == null) {
					throw new IllegalStateException(
						"This is an iterable which bases on some fixed iterator. That iterator was already used.");
				}
				Iterator<T> result = data;
				data = null;
				return result;
			}
		};
	}

	/** @see CollectionUtilShared#toListIterable(Iterable) */
	public static <T> ArrayList<T> toListIterable(Iterable<? extends T> source) {
		return CollectionUtilShared.toListIterable(source);
	}

	/** @see CollectionUtilShared#toList(Iterator) */
	public static <T> ArrayList<T> toList(Iterator<? extends T> iterator) {
		return CollectionUtilShared.toList(iterator);
	}

	/** @see CollectionUtilShared#toSetIterable(Iterable) */
	public static <T> HashSet<T> toSetIterable(Iterable<? extends T> source) {
		return CollectionUtilShared.toSetIterable(source);
	}

	/** @see CollectionUtilShared#toSet(Iterator) */
	public static <T> HashSet<T> toSet(Iterator<? extends T> iterator) {
		return CollectionUtilShared.toSet(iterator);
	}

}
