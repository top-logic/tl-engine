/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOReference;

/**
 * {@link AttributeStorage} for {@link MOReference}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ReferenceStorage extends AttributeStorage {

	/**
	 * Returns the {@link ObjectKey} of the referenced object or <code>null</code> if currently no
	 * value is set.
	 * 
	 * A call to this method does <b>not</b> cause the given reference attribute to load the
	 * referenced object.
	 * 
	 * @param attribute
	 *        the attribute to get the referenced key for.
	 * @param item
	 *        The item to get reference from.
	 * @param storage
	 *        the storage object to resolve the value from
	 * @param contextKey
	 *        Informations that are not not locally stored are taken from that key.
	 * 
	 * @return an {@link ObjectKey} representing the references object
	 */
	ObjectKey getReferenceValueKey(MOReference attribute, DataObject item, Object[] storage, ObjectKey contextKey);

}

