/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.IGroup;

/**
 * The DispatchingSecurityProvider can be used in TableComponents (to protect table columns) and in other scenarios
 * to protect the access to other properties by dispatching the access check to an AccessChecker registered for the property.
 * 
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class DispatchingSecurityProvider implements SecurityProvider {

	private final Map<String, AccessChecker> checkerByProperty;
    
	/**
	 * @param aCheckerByProperty   the checkers to use for the different properties
	 */
    public DispatchingSecurityProvider(Map<String, AccessChecker> aCheckerByProperty) {
        this.checkerByProperty = new HashMap<>(aCheckerByProperty);
    }

	@Override
	public Set<BoundCommandGroup> getAccessRights(Object object, String property, IGroup group) {
		AccessChecker accessChecker = this.getAccessChecker(property);
		if (accessChecker == null) {
			return LiberalAccessChecker.ALL_RIGHTS;
		} else {
			return accessChecker.getAccessRights(object, group);
		}
	}

	@Override
	public boolean hasAccessRight(Object object, String property, IGroup group, BoundCommandGroup accessRight) {
		AccessChecker accessChecker = this.getAccessChecker(property);
		if (accessChecker == null) {
			return true;
		} else {
			return accessChecker.hasAccessRight(object, group, accessRight);
		}
	}

	@Override
	public boolean isRestricted(Object object, String property) {
		AccessChecker accessChecker = this.getAccessChecker(property);
		return accessChecker != null;
	}
	
	/**
	 * Get the AccessChecker for the given property
	 * 
	 * @param aProperty the property
	 * @return the AccessChecker. May be <code>null</code>.
	 */
	protected AccessChecker getAccessChecker(String aProperty) {
		return this.checkerByProperty.get(aProperty);
	}

}
