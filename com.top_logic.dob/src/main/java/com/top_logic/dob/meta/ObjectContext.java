/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.meta;

import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.ObjectKey;

/**
 * The {@link ObjectContext} is needed in {@link MOAttribute} to get information
 * about the context in which values are requested and stored.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ObjectContext extends IdentifiedObject {

	/**
	 * The repository to resolve {@link MetaObject}s valid for the context in
	 * which the data are accessed
	 */
	MORepository getTypeRepository();
	
	/**
	 * Method to resolve the object which has the given <code>objectKey</code>
	 * as object key
	 */
	IdentifiedObject resolveObject(ObjectKey objectKey);
	
	/**
	 * Returns an {@link ObjectKey} which represents the same object, but which
	 * is already known by the system.
	 * 
	 * This method can be used to get a shared variant of the given key to omit
	 * duplicate keys for the same object.
	 * 
	 * @param key
	 *        the key to replace by a known one
	 *        
	 * @return a known key which represents the same object or the given key if
	 *         no equal key is known.
	 */
	ObjectKey getKnownKey(ObjectKey key);

}

