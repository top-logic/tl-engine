/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person;

import com.top_logic.basic.config.annotation.Name;

/**
 * {@link Enum} describing the requirement of multi-factor authentication for a {@link Person}.
 */
public enum MfaRequirement {

	/**
	 * Multi-factor authentication is optional. It can be enabled or disabled.
	 */
	@Name("optional")
    OPTIONAL,

	/**
	 * Multi-factor authentication is required. It cannot be disabled.
	 */
	@Name("required")
	REQUIRED,

	/**
	 * Multi-factor authentication is disabled. It can not be disabled.
	 */
	@Name("disabled")
    DISABLED

}