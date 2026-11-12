/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.selfservice.invitation;

import com.top_logic.basic.config.annotation.Name;

/**
 * Whether and how visitors register an account of their own without an invitation.
 *
 * @see SelfRegistration
 */
public enum SelfRegistrationMode {

	/**
	 * Visitors cannot register an account.
	 */
	@Name("off")
	OFF,

	/**
	 * A registered account can log in right away.
	 */
	@Name("direct")
	DIRECT,

	/**
	 * A registered account is locked until an administrator approves the registration.
	 */
	@Name("approval")
	APPROVAL,

}
