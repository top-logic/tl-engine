/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.core.util;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.core.TLElementVisitor;

/**
 * Collect all visited elements in a List.
 * 
 * @author    <a href="mailto:kbu@top-logic.com>Karsten Buch</a>
 */
public class AllElementVisitor implements TLElementVisitor {
    
    /** stores all visited elements */
    protected List<StructuredElement> list;

    /**
     * Default CTor
     */
    public AllElementVisitor() {
        list = new ArrayList<>();
    }

    /**
     * Allow preallocation of internal List by estimatedSize.
     */
    public AllElementVisitor(int estimatedSize) {
        list = new ArrayList<>(estimatedSize);
    }

    /**
     * Just add anElement to the internal List.
     */
    @Override
	public boolean onVisit(StructuredElement anElement, int aDepth) {
        return this.list.add(anElement);
    }
    
    /**
     * Return the collected Elements.
     * 
     * @return list of elements
     * 
     * @deprecated please use (typed) getElements()
     */
    @Deprecated
    public List getList() {
        return this.list;
    }
    
    /**
     * Return the collected Elements.
     * 
     * @return list of elements
     */
    public List<StructuredElement> getElements() {
        return this.list;
    }

}
