/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.security;

import java.util.Set;

import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.util.Utils;

/**
 * Container for a value that requires special {@link #getAccessRights()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ProtectedValue {

	private final Set/*<BoundCommandGroup>*/ accessRights;
	private final Object value;

	public ProtectedValue(Set accessRights, Object value) {
		this.accessRights = accessRights;
		this.value = Utils.createSortedListForDisplay(value);
	}

	/**
	 * The access rights (set of {@link BoundCommandGroup}s) the current user has on the value.
	 */
	public Set/*<BoundCommandGroup>*/ getAccessRights() {
		return accessRights;
	}

	/**
	 * The protected value itself.
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Whether the current user has the given access right on the value.
	 */
	public boolean hasAccessRight(BoundCommandGroup requiredRight) {
		return accessRights.contains(requiredRight);
	}
	
}
