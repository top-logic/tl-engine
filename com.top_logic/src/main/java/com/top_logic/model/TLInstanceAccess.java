/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

import com.top_logic.basic.col.CloseableIterator;

/**
 * {@link TLQuery} that retrieves instances for model elements.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TLInstanceAccess extends TLQuery {

	/**
	 * {@link CloseableIterator} over all direct instances (excluding subclass instances) of the
	 * given concrete class.
	 * 
	 * @param concreteClass
	 *        The class to retrieve instances from.
	 * @return Instance iterator. Note: The result iterator must be closed after usage.
	 */
	CloseableIterator<TLObject> getAllDirectInstances(TLClass concreteClass);

	/**
	 * {@link CloseableIterator} over all instances (including subclass instances) of the given
	 * class.
	 * 
	 * @param classType
	 *        The class to retrieve instances from.
	 * @return Instance iterator. Note: The result iterator must be closed after usage.
	 */
	CloseableIterator<TLObject> getAllInstances(TLClass classType);

}
