/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.explain;

import java.util.List;

import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * A role an account holds on the object an {@link AccessExplanation} is about, with the entries
 * explaining why.
 *
 * @param role
 *        The role held on the {@link AccessExplanation#decidingObject() deciding object}.
 * @param sources
 *        The entries delivering the role, on the deciding object itself or on one of its
 *        {@link AccessExplanation#roleParents() role parents}. Empty when the
 *        {@link AccessExplanation#sourcesAvailable() sources are not available}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public record HeldRole(BoundedRole role, List<RoleAssignment> sources) {
	// Pure value.
}
