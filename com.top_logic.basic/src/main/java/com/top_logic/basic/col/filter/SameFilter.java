/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter;

import com.top_logic.basic.col.Filter;

/**
 * {@link Filter} accepting only a single given object.
 *
 * @author <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class SameFilter extends AbstractReplaceableSearchObjectFilter {

    /**
     * Default constructor.
     * 
     * @param anObject The object to be used for testing, may be null
     */
    public SameFilter(Object anObject) {
    	super();
        setSearchObject(anObject);
    }

    /**
     * true when object same as anObject.
     */
    @Override
	public boolean accept(Object anObject) {
        return getSearchObject() == anObject;
    }
}
