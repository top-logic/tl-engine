/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.workItem.favorites;

import java.util.Collection;
import java.util.HashSet;

import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.query.BasicCollectionFilter;
import com.top_logic.util.TLContext;

/**
 * The FavoritesFilter keeps all objects marked as favorites
 *
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class FavoritesFilter extends BasicCollectionFilter {

    /**
     * @see com.top_logic.element.meta.query.BasicCollectionFilter#getSortOrder()
     */
    @Override
	public Integer getSortOrder() {
		return Integer.valueOf(666);
    }

    /**
     * @see com.top_logic.element.meta.query.CollectionFilter#filter(java.util.Collection)
     */
    @Override
	public Collection filter(Collection aCollection) throws NoSuchAttributeException,
            AttributeException {

        Collection theFavorites = FavoritesUtils.getFavoriteSubjects(TLContext.getContext().getCurrentPersonWrapper());

        Collection theResult = new HashSet(aCollection);
        theResult.retainAll(theFavorites);

        return theResult;
    }

}

