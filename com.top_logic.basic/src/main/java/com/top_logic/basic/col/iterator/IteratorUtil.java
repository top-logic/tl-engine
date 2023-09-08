/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.iterator;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

import org.apache.commons.collections4.iterators.ArrayIterator;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.DynamicCastMapping;
import com.top_logic.basic.col.MappedIterable;
import com.top_logic.basic.shared.collection.iterator.IteratorUtilShared;

/**
 * Utilities for working with {@link Iterator}s.
 * 
 * @see IteratorUtilShared
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class IteratorUtil extends IteratorUtilShared {

	/**
	 * @see EnumerationAdapterIterator
	 * @see EnumerationAdapterIterator#EnumerationAdapterIterator(Enumeration)
	 */
	public static <T> Iterator<T> fromEnumeration(Enumeration<T> enumeration) {
		return new EnumerationAdapterIterator<>(enumeration);
	}

	/**
	 * @see IteratorAdapterEnumeration
	 * @see IteratorAdapterEnumeration#IteratorAdapterEnumeration(Iterator)
	 */
	public static <T> Enumeration<T> toEnumeration(Iterator<T> iterator) {
		return new IteratorAdapterEnumeration<>(iterator);
	}

	/**
	 * Creates an iterator over the given object. 
	 * 
	 * The object to iterate over must be iterable (currently a collection or an array).
	 *
	 * @param aObject
	 *        the object to get an iterator over
	 * @return an iterator over the given object
	 */
	public static Iterator<?> getIterator(Object aObject) {
		if (aObject instanceof Iterable<?>) {
			return ((Iterable<?>)aObject).iterator();
		}
		else if (aObject == null) {
			return EMPTY_ITERATOR;
		}
		else if (aObject.getClass().isArray()) {
			return new ArrayIterator<>(aObject);
		}
		else if (aObject instanceof Iterator<?>) {
			return (Iterator<?>) aObject;
		}
		else throw new IllegalArgumentException("The given object is not iterable: " + aObject);
	}

	/**
	 * @see AppendIterator
	 * @see AppendIterator#AppendIterator()
	 */
	public static <T> AppendIterator<T> createAppendIterator() {
		return new AppendIterator<>();
	}

	/**
	 * Safely cast an {@link Iterable} to an {@link Iterable} of generic type with runtime check
	 * (at iteration time).
	 * 
	 * @see CollectionUtil#containsOnly(Class, Collection)
	 * @see CollectionUtil#copyOnly(Class, Collection)
	 * 
	 * @param <T>
	 *        The type to cast to.
	 * @param expectedType
	 *        The expected runtime type.
	 * @param values
	 *        The values to cast.
	 * @return view to the casted values.
	 * 
	 * @throws NullPointerException if the given Iterable or 'expectedType' is <code>null</code>.
	 */
	public static <T> Iterable<T> dynamicCastView(Class<T> expectedType, Iterable<?> values) {
		if (expectedType == null) {
			// Fail early
			throw new NullPointerException("The given 'expectedType' is null!");
		}
		if (values == null) {
			// Fail early
			throw new NullPointerException("The given Iterable is null!");
		}
		return new MappedIterable<>(new DynamicCastMapping<>(expectedType), values);
	}

	/** Wraps the given {@link Iterator} into an {@link UnmodifiableIterator}. */
	public static <E> UnmodifiableIterator<E> unmodifiableIterator(Iterator<? extends E> iterator) {
		return new UnmodifiableIterator<>(iterator);
	}

}
