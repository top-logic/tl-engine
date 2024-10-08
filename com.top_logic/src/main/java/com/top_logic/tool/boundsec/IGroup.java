/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.util.Collection;

import com.top_logic.basic.Named;
import com.top_logic.basic.TLID;
import com.top_logic.tool.boundsec.wrap.GroupMember;

/**
 * A Group of members that are BoundObjects (normally Persons).
 * 
 * @author    <a href="mailto:kbu@top-logic.com>Karschten Busch</a>
 */
public interface IGroup extends Named, GroupMember {

	/**
	 * Get the ID of this group
	 * 
	 * @return the ID of this group, <code>null</code> if ID could not be retrieved
	 */
    public TLID getID();
    
    /**
     * Get all the members of this Group
     * 
     * @return the members. May be empty but not <code>null</code>
     */
    public Collection getMembers();

}
