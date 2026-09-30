/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.security;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * How a command is displayed that the model access rights refuse to the current user.
 *
 * @see ModelAccessPolicy
 */
public enum DeniedDisplay implements ExternallyNamed {

	/**
	 * The command is not displayed at all.
	 */
	HIDE("hide"),

	/**
	 * The command is displayed but cannot be executed; it gives the refusal as its reason.
	 */
	DISABLE("disable");

	private final String _externalName;

	private DeniedDisplay(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}
}
