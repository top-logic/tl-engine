/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.query;

import java.util.Collection;
import java.util.Map;

import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeException;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class TrueCollectionFilter extends BasicCollectionFilter {

    /** 
     * Creates a {@link TrueCollectionFilter}.
     * 
     */
    public TrueCollectionFilter() {
    }

    /** 
     * Creates a {@link TrueCollectionFilter}.
     */
    public TrueCollectionFilter(Map aValueMap) throws IllegalArgumentException {
        super(aValueMap);
    }

    /** 
     * Creates a {@link TrueCollectionFilter}.
     */
    public TrueCollectionFilter(boolean aDoNegate, boolean aIsRelevant) {
        super(aDoNegate, aIsRelevant);
    }

    /**
     * @see com.top_logic.element.meta.query.BasicCollectionFilter#getSortOrder()
     */
    @Override
	public Integer getSortOrder() {
        return Integer.valueOf(1000);
    }

    /**
     * @see com.top_logic.element.meta.query.CollectionFilter#filter(java.util.Collection)
     */
    @Override
	public Collection filter(Collection aCollection) throws NoSuchAttributeException,
            AttributeException {
        return aCollection;
    }

}

