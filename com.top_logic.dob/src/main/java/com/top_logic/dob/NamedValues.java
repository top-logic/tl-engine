/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob;

import com.top_logic.dob.ex.IncompatibleTypeException;
import com.top_logic.dob.ex.NoSuchAttributeException;

/**
 * Untyped super interface for {@link DataObject}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface NamedValues {

	/**
	 * The list of attribute names.
	 * 
	 * @return The list of known attribute names.
	 */
	public String [] getAttributeNames ();

	/***
	 * Return the value for the given Attribute.
	 * 
	 * @param attrName
	 *        the name of the attribute.
	 * @return the value of the attribute.
	 * 
	 * @throws NoSuchAttributeException
	 *         in case such an attribute does not exist.
	 * 
	 * @see #hasAttribute(String)
	 */
   public Object getAttributeValue (String attrName)
       throws NoSuchAttributeException;

	/**
	 * Sets the value for the given Attribute
	 * 
	 * @param attrName
	 *        the name of the attribute to set.
	 * @param value
	 *        the new value of the attribute.
	 * @return The old value that was replaced.
	 * 
	 * @throws IncompatibleTypeException
	 *         when value has wrong type
	 * @throws NoSuchAttributeException
	 *         in case such an attribute does not exist
	 * 
	 * @see #hasAttribute(String)
	 */
   public Object setAttributeValue (String attrName, Object value)
       throws DataObjectException;

	/**
	 * Determines whether an attribute with the given name is available. If this method returns
	 * <code>true</code>, then {@link #getAttributeValue(String)} and
	 * {@link #setAttributeValue(String, Object)} do not throw {@link NoSuchAttributeException}.
	 * 
	 * @param attributeName
	 *        The name of the attribute to access
	 */
	boolean hasAttribute(String attributeName);
}
