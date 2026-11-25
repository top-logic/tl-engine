/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.rule.config;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * The data a role rule or role parent rule is evaluated on, when the checked object is a historic
 * version of an object.
 *
 * <p>
 * On a current object, a rule navigates its path in the current data regardless of the mode, with
 * the exception of {@link #ANCHORED}, which grants nothing there.
 * </p>
 *
 * @see NavigationRuleConfig#getHistory()
 */
public enum HistoryMode implements ExternallyNamed {

	/**
	 * The path is navigated in the current data, also when a historic version is checked: The roles
	 * of the identity hold for each version of the object.
	 */
	CURRENT("current"),

	/**
	 * The path is navigated in the revision of the checked version.
	 */
	HISTORIC("historic"),

	/**
	 * The path is navigated in the revision of the checked version, but the rule only applies to a
	 * version that is covered by a current object through one of the
	 * {@link NavigationRuleConfig#getAnchors() anchor references} of the rule. The rule grants
	 * nothing on a current object.
	 */
	ANCHORED("anchored"),

	;

	private final String _externalName;

	private HistoryMode(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

}
