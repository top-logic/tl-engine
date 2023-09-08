/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

/**
 * Mechanism to externally associate objects with string-based identifiers.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface IdentityProvider {

	/**
	 * The identifier associated with the given object.
	 * 
	 * @param object
	 *        The object whose identity is requested.
	 * @return The identifier for the given object.
	 */
	String getObjectId(Object object);
	
	/**
	 * Lookup the object that is associated with the given identifier.
	 * 
	 * @param id
	 *        The identifier to lookup the associated object for.
	 * @return The object that is identified by the given ID. <code>null</code>,
	 *         if the given ID is not known.
	 */
	Object getObjectById(String id);

}
