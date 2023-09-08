/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task;

import java.util.Comparator;

import com.top_logic.basic.CollectionUtil;

/**
 * Comparator for implementations of <code>Task</code>.
 *
 * @author    <a href="mailto:asc@top-logic.com">Alice Scheerer</a>
 */
public class TaskComparator implements Comparator<Task> {
    
    /** Need just this instance of the Comparator */
    public static final TaskComparator INSTANCE = new TaskComparator();

    private TaskComparator() {
    	//singleton
    }
    
    /**
	 * Compares two {@link Task}.
	 * 
	 * The method first compares by {@link Task#getNextShed()}, then by {@link Task#getName()}.
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
    @Override
	public int compare(Task o1, Task o2) {
        // first check, if the objects are same (or both null)
        if (o1 == o2) {
            return 0;
        }
        
        if (o1 == null) {
            return -1;
        }
        
        if (o2 == null) {
            return 1;
        }
        
        long nextShed        = o1.getNextShed();
        long other           = o2.getNextShed();
        
        String theName       = o1.getName();
        String theOtherName  = o2.getName();

        // can not return nextShed - other due to integer overflow
        if (nextShed > other) {
            return 1;
        }
        if (nextShed < other) {
            return -1;
        }
        
        // now check, if the names are same (or both null)
		return CollectionUtil.compareComparableNullIsSmaller(theName, theOtherName);
    }

}
