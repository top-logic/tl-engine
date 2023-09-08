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
 * Algorithm checking access rights to object attributes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface SecurityProvider {

	public boolean isRestricted(Object object, String property);
	
	public Set<BoundCommandGroup> getAccessRights(Object object, String property, IGroup group);
	
	public boolean hasAccessRight(Object object, String property, IGroup group, BoundCommandGroup accessRight);
	
}
