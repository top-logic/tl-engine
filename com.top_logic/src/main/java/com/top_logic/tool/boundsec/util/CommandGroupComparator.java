/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.util;

import java.util.Comparator;

import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Compares command groups according to their dependency. 
 * 
 * We uses some default command groups and may use
 * arbitrarry command groups in specific components. The 'read' command group
 * depends (acording to this comparator) on the 'read' command group,
 * the 'delete'command group in addition depends on the 'write' command group.
 * All other command groups equal the 'write' command group in terms of dependency. 
 * 
 * The comarator works on command groups on command group ids, or a mix of them.
 * 
 * NOTE! This comparator is NOT total and NOT consistent with equals().
 * 
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 * 
 */
public class CommandGroupComparator implements Comparator {
    
    /** Can use this single Insatnce for now */
    public static CommandGroupComparator INSTANCE = new CommandGroupComparator(true);
    
    /** This defines the order of the BoundCommandGroups */
    public static final String[] groups = {
        SimpleBoundCommandGroup.READ  .getID(),
        SimpleBoundCommandGroup.WRITE .getID(),
        SimpleBoundCommandGroup.DELETE.getID()
    };

    /**
     * Just to allow this to be private.
     */
    private CommandGroupComparator(boolean xxx) {
        super();
    }

    /** 
     * Commpare two command groups according to their dependency.
     * 
     * @param     o1    The first object to be compared.
     * @param     o2    The second object to be compared.
     * @return    A negative integer, zero, or a positive integer as 
     *            the first argument is less than, equal to, or greater than the second.
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
	public int compare(Object o1, Object o2) {
        // same objects are equal
        if (o1 == o2) 
            return 0;
        
        // null depends on nothing
        if (o1 == null) 
            return -1;

        // convert given command groups to their id
        if (o1 instanceof BoundCommandGroup) {
            o1 = ((BoundCommandGroup) o1).getID();
        }
        if (o2 instanceof BoundCommandGroup) {
            o2 = ((BoundCommandGroup) o2).getID();
        }
        int theIndex1 = indexOf(o1);
        int theIndex2 = indexOf(o2);
        if (theIndex1 < 0 || theIndex2 < 0) {
            // on of the command groups is not a standart command group
            if (theIndex1 == 0) {
                // the first command group is the read group
                return 1;
            } else if (theIndex2 == 0) {
                // the second command group is the read group
                return -1;
            } 
            // otherwise, the command groups are relatively equal
            return 0;
        }
        // for standard command groups, the difference between the indices provide the correct sign
        return theIndex2 - theIndex1;
    }

    /**
     * Map the CommandGroups IDs to an index.
     */
    private int indexOf(Object anId) {
        int i=0;
        for (; i<groups.length; i++) {
            if (groups[i].equals(anId)) return i;
        }
        // anyhing not in the list is technically equal to write
        return -1;
    }
}
