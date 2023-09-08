/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.query;

import java.util.Collection;

import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.knowledge.wrap.mapBasedPersistancy.MapBasedPersistancyAware;

/**
 * Filter for collections of objects.
 * 
 * @author <a href="mailto:fma@top-logic.com">Frank Mausz</a>
 */
public interface CollectionFilter<C> extends Comparable<C>, MapBasedPersistancyAware {

	/**
	 * Filters the given collection according to the filter and returns a new collection with all
	 * matching elements.
	 * 
	 * <p>
	 * The result is always a Collection, never <code>null</code>.
	 * </p>
	 *
	 * @param aCollection
	 *        Objects to be filtered.
	 * @return All matching elements in a new collection.
	 */
	Collection<C> filter(Collection<C> aCollection) throws NoSuchAttributeException, AttributeException;
}
