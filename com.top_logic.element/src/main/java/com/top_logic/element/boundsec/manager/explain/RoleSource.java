/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.explain;

import com.top_logic.element.boundsec.manager.rule.RoleProvider;
import com.top_logic.knowledge.security.SecurityStorage;

/**
 * Why an account holds a role on an object, as recorded in the security storage.
 *
 * @see RoleAssignment#source()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum RoleSource {

	/**
	 * The role is assigned to the account or one of its groups directly on the object.
	 *
	 * @see SecurityStorage#REASON_HAS_ROLE
	 */
	DIRECT,

	/**
	 * The role is delivered by a role rule of the access configuration.
	 *
	 * @see RoleAssignment#rule()
	 */
	RULE,

	/**
	 * The security storage names a reason for which no role rule of the running configuration is
	 * known, e.g. a rule that was removed after the storage was built.
	 *
	 * @see RoleProvider
	 */
	UNKNOWN;

}
