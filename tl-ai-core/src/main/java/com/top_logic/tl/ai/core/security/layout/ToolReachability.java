/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

/**
 * Who reaches a tool under the policy.
 */
/*
 * The three states worth telling apart in the catalog. "Everyone" is not the same as a list of every
 * configured role: the baseline reaches accounts that hold no role at all, and a policy whose baseline
 * grants a capability has said something about every future role too. NOBODY is the state that needs an
 * administrator, and it is the reason this is a type and not just a joined list of names - an empty list
 * would render as an empty cell.
 */
public enum ToolReachability {

	/** Every account, because the baseline grants it. */
	EVERYONE,

	/** Only the accounts holding one of the roles the policy names. */
	NAMED_ROLES,

	/** Nobody but an administrator. */
	NOBODY;

}
