/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import java.util.Collection;

import com.top_logic.basic.col.FixedRangeIterator;
import com.top_logic.basic.col.IDRangeIterator;
import com.top_logic.knowledge.service.KBUtils;

/**
 * A Generic {@link IDRangeIterator} for Wrappers.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class WrapperRangeIterator extends FixedRangeIterator {

    /** 
     * Create a new WrapperRangeIterator for a Collection of Wrappers.
     */
    public WrapperRangeIterator(Collection aCollection) {
        super(aCollection);
    }

    /** 
     * Create a new WrapperRangeIterator for an Array of Wrappers.
     */
    public WrapperRangeIterator(Wrapper[] aAnObjectArray) {
        super(aAnObjectArray);
    }

    /**
     * @see com.top_logic.basic.col.IDRangeIterator#getIDFor(java.lang.Object)
     */
    @Override
	public Object getIDFor(Object obj) {
        return KBUtils.getWrappedObjectName(((Wrapper) obj));
    }

    /**
     * @see com.top_logic.basic.col.IDRangeIterator#getUIStringFor(java.lang.Object)
     */
    @Override
	public String getUIStringFor(Object obj) {
        return ((Wrapper) obj).getName();
    }

}

