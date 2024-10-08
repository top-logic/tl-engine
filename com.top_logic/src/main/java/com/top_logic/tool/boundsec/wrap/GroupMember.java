/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.wrap;

import java.util.Set;

import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.model.TLObject;

/**
 * Objects that can be members of a {@link Group}.
 */
public interface GroupMember extends TLObject {

	/**
	 * All direct {@link Group}s assigned to this account.
	 */
	default Set<Group> getGroups() {
		return AbstractWrapper.resolveWrappersTyped(this, Group.GROUPS_ATTR, Group.class);
	}

}
