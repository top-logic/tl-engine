/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.Collection;

import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.ex.IncompatibleTypeException;
import com.top_logic.knowledge.service.FlexDataManager;

/**
 * Value holder for data loaded by {@link FlexDataManager}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface FlexData {
	
	/**
	 * Returns the collection of available attributes. The collection must not be modified.
	 */
	Collection<String> getAttributes();

	/**
	 * Return the value for the given Attribute.
	 * 
	 * @param attributeName
	 *        the name of the attribute.
	 * 
	 * @return The value of the attribute. If there is no such attribute, <code>null</code> is
	 *         returned.
	 */
	Object getAttributeValue(String attributeName);

	/**
	 * Sets the value for the given Attribute
	 * 
	 * @param attributeName
	 *        the name of the attribute to set.
	 * @param value
	 *        the new value of the attribute.
	 * @return The old value that was replaced.
	 * 
	 * @throws IncompatibleTypeException
	 *         when value has wrong type
	 */
	public Object setAttributeValue(String attributeName, Object value)
			throws DataObjectException;

	/**
	 * Determines whether an attribute with the given name is available.
	 * 
	 * @param attributeName
	 *        The name of the attribute to access
	 */
	boolean hasAttribute(String attributeName);

	/**
	 * Revision at which the given attribute was last modified.
	 * 
	 * @param attributeName
	 *        Attribute to get "last modified revision" for.
	 * @return The commit number of the revision in which the attribute was last modified, or
	 *         <code>-1</code> if there is no such attribute.
	 * 
	 * @see #hasAttribute(String)
	 */
	long lastModified(String attributeName);

}

