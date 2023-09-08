/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.misc;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationListener;
import com.top_logic.basic.config.PropertyDescriptor;

/**
 * One property of an {@link ConfigurationItem} instance.
 * <p>
 * Difference to {@link PropertyDescriptor}: A {@link PropertyDescriptor} instance represents all
 * instances of the property on every {@link ConfigurationItem} that has this property. It is the
 * model and implementation. A {@link PropertyValue} instance represents a property of a single
 * {@link ConfigurationItem} instance. It has methods to get and set the value, register listeners
 * and so on.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface PropertyValue {

	/**
	 * The descriptor of this property, never null.
	 */
	PropertyDescriptor getProperty();

	/**
	 * The {@link ConfigurationItem}, never null.
	 */
	ConfigurationItem getItem();

	/**
	 * Looks up the value of this property.
	 * 
	 * @see ConfigurationItem#value(PropertyDescriptor)
	 */
	Object get();

	/**
	 * Whether this property got on creation an explicit value.
	 * 
	 * @see ConfigurationItem#valueSet(PropertyDescriptor)
	 */
	boolean isSet();

	/**
	 * Updates the value of the given property with the given value.
	 * 
	 * @see ConfigurationItem#update(PropertyDescriptor, Object)
	 * 
	 * @param value
	 *        the new value
	 * 
	 * @return the old value or <code>null</code> if none was set before.
	 */
	Object set(Object value);

	/**
	 * Resets the value of this property.
	 * <p>
	 * After this call, the property will behave as if it has never been set.
	 * </p>
	 * 
	 * @see ConfigurationItem#reset(PropertyDescriptor)
	 */
	void reset();

	/**
	 * Adds the given {@link ConfigurationListener} to this property.
	 * 
	 * @param listener
	 *        The {@link ConfigurationListener} to notify upon changes.
	 * @return Whether the listener was actually added. <code>false</code> if the listener was
	 *         already registered.
	 */
	boolean addListener(ConfigurationListener listener);

	/**
	 * Removes a {@link ConfigurationListener} from this property.
	 * 
	 * @param listener
	 *        The {@link ConfigurationListener} to remove.
	 * @return Whether the listener was actually removed. <code>false</code>, if the listener was
	 *         not registered.
	 */
	boolean removeListener(ConfigurationListener listener);

}
