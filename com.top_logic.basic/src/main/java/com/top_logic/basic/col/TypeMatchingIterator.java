/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Iterator;

/**
 * {@link TransformIterator} that only delivers objects of the source iterator that are assignment
 * compatible to a given {@link Class type}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TypeMatchingIterator<D> extends TransformIterator<Object, D> {

	private final Class<D> _type;

	/**
	 * Creates a new {@link TypeMatchingIterator}.
	 * 
	 * @param matchingType
	 *        The type the object in the source must have to be delivered.
	 * @param source
	 *        The source object to filter.
	 * 
	 * @see FilterUtil#filterIterator(Class, Iterator)
	 */
	TypeMatchingIterator(Class<D> matchingType, Iterator<? extends Object> source) {
		super(source);
		_type = matchingType;
	}

	@Override
	protected boolean test(Object value) {
		return value == null || _type.isInstance(value);
	}

	@Override
	protected D transform(Object value) {
		return _type.cast(value);
	}

	@Override
	protected boolean acceptDestination(D value) {
		return true;
	}

}

