/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

/**
 * An accessor for a single property of an object.
 * 
 * @param <T> The target type this {@link PropertyAccessor} can access.
 * 
 * @see Accessor for an interface accessing multiple properties. An
 *      {@link Accessor} can be build from multiple {@link PropertyAccessor}s.
 * 
 * @see DispatchingAccessor for an {@link Accessor} implementation that relies
 *      on a set of {@link PropertyAccessor}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface PropertyAccessor<T> {

	/**
	 * Reads some property of the given target object.
	 * 
	 * @param target
	 *        The object that is accessed.
	 * @return The value of the property of the given target object that this
	 *         accessor reads.
	 */
	public Object getValue(T target);

	/**
	 * Writes some property of the given target object.
	 * 
	 * @param target
	 *        The object that is accessed.
	 * @param newValue
	 *        The new value, to which the property of the given target object is
	 *        set.
	 */
	public void setValue(T target, Object newValue);
	
}
