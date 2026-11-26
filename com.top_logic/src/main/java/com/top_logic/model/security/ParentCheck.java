/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.BoundCommandGroup;

/**
 * The check of an operation on one access parent of an object, which an
 * {@link AccessParentFunction} combines into the decision for the object, see
 * {@link AccessParentFunction#allows(TLObject, BoundCommandGroup, ParentCheck)}.
 */
@FunctionalInterface
public interface ParentCheck {

	/**
	 * Whether the operation is allowed on the given access parent.
	 *
	 * @param parent
	 *        An access parent of the delegating object.
	 * @param operation
	 *        The operation on the delegating object. The check maps it to the operation it stands
	 *        for on the parent itself.
	 * @return Whether the parent allows the operation.
	 */
	boolean test(TLObject parent, BoundCommandGroup operation);

}
