/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration;

import java.util.Iterator;

import com.top_logic.dob.MetaObject;
import com.top_logic.knowledge.event.ObjectCreation;

/**
 * Holder for creations of unversioned objects.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface UnversionedCreations {

	/**
	 * The type of the Unversioned objects.
	 */
	MetaObject getType();

	/**
	 * The {@link ObjectCreation creations}.
	 */
	Iterator<ObjectCreation> getItems();

}

