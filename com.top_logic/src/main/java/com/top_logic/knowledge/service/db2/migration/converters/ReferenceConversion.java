/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.converters;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.service.db2.migration.rewriters.ForeignKeyConverter;

/**
 * Helper to transform a self made object reference, i.e. the internal string identifier contained
 * in a string attribute, into an {@link ObjectKey}.
 * 
 * @see ForeignKeyConverter
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ReferenceConversion {

	/**
	 * Resolves the {@link ObjectKey key} of the object identified by the given dump value.
	 * 
	 * @param event
	 *        Currently processed {@link ItemChange}.
	 * @param dumpValue
	 *        The string identifier of an object.
	 * 
	 * @return The {@link ObjectKey} identifying the referenced object.
	 */
	ObjectKey convertReference(ItemChange event, String dumpValue);

	/**
	 * Handle a <code>null</code> reference, i.e. the objects references no object.
	 * 
	 * @param event
	 *        Currently processed {@link ItemChange}.
	 */
	void handleNullReference(ItemChange event);

}
