/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import com.top_logic.basic.config.annotation.Label;

/**
 * Origin of the value of a configuration variable.
 *
 * <p>
 * The origins are listed in the order of precedence: The first origin that delivers a value
 * determines the value of the variable.
 * </p>
 *
 * @see AliasDefinition#getSource()
 */
public enum AliasSource {

	/**
	 * The value is a JNDI value <code>tl/&lt;name&gt;</code> of one of the
	 * {@link AliasDefinition#getEnvironmentNames() environment names} of the variable.
	 */
	@Label("JNDI")
	JNDI,

	/**
	 * The value is a system property of one of the {@link AliasDefinition#getEnvironmentNames()
	 * environment names} of the variable.
	 */
	SYSTEM_PROPERTY,

	/**
	 * The value is an environment variable of one of the
	 * {@link AliasDefinition#getEnvironmentNames() environment names} of the variable.
	 */
	ENVIRONMENT,

	/**
	 * The value is set in the application and stored in the file {@link AliasManager#STORED_VALUES_FILE}.
	 */
	STORED,

	/**
	 * The value is the {@link AliasDefinition#getDefaultValue() default} given in the configuration.
	 */
	DEFINITION;

	/**
	 * Whether the value is delivered by one of the {@link AliasDefinition#getEnvironmentNames()
	 * environment names} of the variable.
	 */
	public boolean isEnvironment() {
		return this == JNDI || this == SYSTEM_PROPERTY || this == ENVIRONMENT;
	}

}
