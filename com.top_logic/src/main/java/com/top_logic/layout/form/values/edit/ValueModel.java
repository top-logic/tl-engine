/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit;

import java.util.Collection;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.misc.PropertyValue;

/**
 * Abstraction for a single model attribute being edited.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ValueModel {

	/**
	 * The {@link PropertyValue} representing the {@link #getProperty() property} on the
	 * {@link #getModel() model}.
	 */
	PropertyValue getPropertyValue();

	/**
	 * The surrounding model object.
	 */
	ConfigurationItem getModel();

	/**
	 * The property of the {@link #getModel() model} being edited.
	 */
	PropertyDescriptor getProperty();

	/**
	 * The actual value.
	 */
	Object getValue();

	/**
	 * Updates the internal value to the newly given value.
	 * 
	 * @param newValue
	 *        The new value to set.
	 */
	void setValue(Object newValue);

	/**
	 * In case of a collection value, adds a new element to the collection.
	 * 
	 * @param newElement
	 *        The new element to add to the collection value.
	 * @return <tt>true</tt> if the collection value changed as a result of the call.
	 */
	boolean addValue(Object newElement);

	/**
	 * In case of a collection value, removes an element from the collection.
	 * 
	 * @param oldElement
	 *        The element to remove from the collection.
	 */
	void removeValue(Object oldElement);

	/**
	 * In case of a {@link Collection} value, removes all elements from the {@link Collection}.
	 */
	void clearValue();

}
