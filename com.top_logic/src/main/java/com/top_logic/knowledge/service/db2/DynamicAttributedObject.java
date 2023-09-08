/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.knowledge.service.FlexDataManager;

/**
 * Interface deciding whether dynamic values must be initialized.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
interface DynamicAttributedObject {

	/**
	 * Whether the dynamic values for the given revision need to be loaded before they can be
	 * accessed.
	 * 
	 * @param dataRevision
	 *        The revision of the data.
	 */
	boolean needsToBeLoaded(long dataRevision);

	/**
	 * Installs the given dynamic values when no dynamic values are loaded. Otherwise nothing
	 * happens.
	 * 
	 * @param dataRevision
	 *        The revision of the data.
	 * @param values
	 *        The new dynamic values.
	 */
	void initDynamicValues(long dataRevision, FlexData values);

	/**
	 * The {@link FlexDataManager} handling the dynamic values.
	 */
	FlexDataManager getFlexDataManager();

}

