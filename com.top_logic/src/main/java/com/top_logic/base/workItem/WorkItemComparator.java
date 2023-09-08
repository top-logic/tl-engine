/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.workItem;

import java.util.Comparator;

import com.top_logic.basic.col.ComparableComparator;

/**
 * {@link Comparator} for {@link WorkItem}s and {@link SubWorkItem}s that
 * establishes an order that asserts that {@link SubWorkItem} always appear
 * after their parent {@link WorkItem}.
 * 
 * <p>
 * Note: This comparator cannot be inverted for sorting in descendant order. A
 * special instance {@link #INSTANCE_DESCENDING} must be used instead.
 * </p>
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class WorkItemComparator implements Comparator {

    public static final WorkItemComparator INSTANCE = 
    	new WorkItemComparator(ComparableComparator.INSTANCE);
    
    public static final WorkItemComparator INSTANCE_DESCENDING = 
    	new WorkItemComparator(ComparableComparator.INSTANCE_DESCENDING);

    private final Comparator itemComparator;
    
    private WorkItemComparator(Comparator itemComparator) {
		this.itemComparator = itemComparator;
	}
    
    @Override
	public int compare(Object item1, Object item2) {
        boolean isO1SubItem = (item1 instanceof SubWorkItem);
        boolean isO2SubItem = (item2 instanceof SubWorkItem);

        if (isO1SubItem && isO2SubItem) {
        	SubWorkItem subItem1 = (SubWorkItem) item1;
        	SubWorkItem subItem2 = (SubWorkItem) item2;
        	
            if (subItem1.parent == subItem2.parent) {
            	// Directly compare the items.
                return itemComparator.compare(item1, item2);
            }
            else {
            	// Compare the parents of the items.
                return itemComparator.compare(subItem1.parent, subItem2.parent);
            }
        }
        else if (isO1SubItem && (! isO2SubItem)) {
        	SubWorkItem subItem1 = (SubWorkItem) item1;
        	
            if (subItem1.getParent() == item2) {
            	// Item 1 must appear after item 2, because item 1 is a child of
				// item 2. Therefore, item1 > item2.
                return 1;
            }
            else {
            	// Compare the child's parent with item 2.
                return itemComparator.compare(subItem1.getParent(), item2);
            }
        }
        else if (isO2SubItem && (! isO1SubItem)) {
        	SubWorkItem subItem2 = (SubWorkItem) item2;
        	
            if (subItem2.getParent() == item1) {
            	// Item 2 must appear after item 1, because item 2 is a child of
				// item 1. Therefore, item1 < item2.
                return -1;
            }
            else {
            	// Compare item 1 with the parent of item 2.
                return itemComparator.compare(item1, subItem2.getParent());
            }
        }
        else {
        	// (! isO1SubItem) && (! isO2SubItem)
        	return itemComparator.compare(item1, item2);
        }
    }
}
