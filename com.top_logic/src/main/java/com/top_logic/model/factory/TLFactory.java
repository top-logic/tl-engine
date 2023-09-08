/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.factory;

import com.top_logic.knowledge.wrap.ValueProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;

/**
 * Factory interface for generic object creation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TLFactory {

	/**
	 * Creates a new object in the context of a given object (e.g. its parent object in a
	 * structure).
	 * 
	 * @param type
	 *        The type of object to create.
	 * @param context
	 *        The context in which the new object is created.
	 * @param initialValues
	 *        A provider for initial values that are required at object creation time.
	 * 
	 * @return The newly created object
	 */
	TLObject createObject(TLClass type, TLObject context, ValueProvider initialValues);

	/**
	 * Creates a new object in the context of a given object (e.g. its parent object in a
	 * structure).
	 * 
	 * @param type
	 *        The type of object to create.
	 * @param context
	 *        The context in which the new object is created.
	 * @return The newly created object
	 */
	default TLObject createObject(TLClass type, TLObject context) {
		return createObject(type, context, null);
	}

	/**
	 * Creates a new object without context.
	 * 
	 * @param type
	 *        The type of object to create.
	 * @return The newly created object
	 */
	default TLObject createObject(TLClass type) {
		return createObject(type, null);
	}

}
