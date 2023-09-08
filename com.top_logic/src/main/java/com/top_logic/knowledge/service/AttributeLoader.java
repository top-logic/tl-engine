/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import com.top_logic.knowledge.service.db2.FlexData;

/**
 * Algorithm to install bulk-loaded flex attributes into their base objects.
 * 
 * @param <T>
 *        The type of base objects to load attribute data for.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface AttributeLoader<T> {

	/**
	 * Installs the given data into the given base object.
	 * 
	 * @param dataRevision
	 *        The revision whose data should be loaded.
	 * @param baseObject
	 *        The object to install loaded data into.
	 * @param data
	 *        The attribute data.
	 */
	void loadData(long dataRevision, T baseObject, FlexData data);

	/**
	 * Notification that there is no persistent attribute data for the given base object.
	 * 
	 * @param dataRevision
	 *        The revision whose data should be loaded.
	 * @param baseObject
	 *        The object without attribute data.
	 */
	void loadEmpty(long dataRevision, T baseObject);

}
