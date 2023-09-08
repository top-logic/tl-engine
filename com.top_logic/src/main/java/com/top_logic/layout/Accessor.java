/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.layout.basic.BeanAccessor;


/**
 * Generic access to a set of object properties.
 * 
 * <p>
 * An implementation of {@link Accessor} may either rely on some canonical
 * mapping of property names to property access functionality for a certain
 * object type, or define this mapping itself.
 * </p>
 * 
 * <p>
 * For an example of a canonical mapping of property names to access
 * functionality see {@link BeanAccessor}.
 * </p>
 * 
 * @param <T> The target type this {@link Accessor} can access.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Accessor<T> {
	/**
	 * Read the given named property from the given object.
	 * 
	 * @param object
	 *     The object to access.
	 * @param property
	 *     The name of the property to read
	 * @return
	 *     The value of the corresponding property.
	 */
	public Object getValue(T object, String property);
	
	/**
	 * Set the given property of the given object to the given value.
	 * 
	 * @param object
	 *     The object to access.
	 * @param property
	 *     The name of the property to set.
	 * @param value
	 *     The new value to set the given property to.
	 */
	public void setValue(T object, String property, Object value);
}
