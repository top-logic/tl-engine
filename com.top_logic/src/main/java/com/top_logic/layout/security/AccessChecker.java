/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.security;

import java.util.Set;

import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.IGroup;

/**
 * The AccessChecker delivers the access rights of a group on an object.
 * Note that the checker does not enforce access restrictions.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface AccessChecker {
	
	/**
	 * Get the access rights of the given group to the given object as a Set of BoundCommandGroups
	 * 
	 * @param object	the object
	 * @param group		the group (usually the representative group of the current user)
	 * @return the access rights as defined above
	 */
	public Set<BoundCommandGroup> getAccessRights(Object object, IGroup group);

	/**
	 * Check if the given group has the given access right on the given object
	 * 
	 * @param object		the object
	 * @param group			the group (usually the representative group of the current user)
	 * @param accessRight	the access right
	 * @return true if the access is granted
	 */
	public boolean hasAccessRight(Object object, IGroup group,	BoundCommandGroup accessRight);
	
}
