/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.security;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * How a UI unit with an {@link AccessControl} is displayed while its operation is refused on the
 * object it checks.
 *
 * @see AccessControl#getDenied()
 */
public enum DeniedUnitDisplay implements ExternallyNamed {

	/**
	 * The unit is not displayed: a tile leaves no gap, a tab, an accordion section and a sidebar
	 * entry are left out of their strip or list. The unit is displayed again as soon as its object
	 * is allowed again.
	 *
	 * <p>
	 * A selected tab that is hidden passes the selection to the nearest visible tab. An open
	 * navigation item that is hidden stays open and shows the "no access" placeholder, since the
	 * user navigated there.
	 * </p>
	 */
	HIDE("hide"),

	/**
	 * The unit stays displayed and shows a "no access" placeholder instead of its content.
	 */
	PLACEHOLDER("placeholder");

	private final String _externalName;

	private DeniedUnitDisplay(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

}
