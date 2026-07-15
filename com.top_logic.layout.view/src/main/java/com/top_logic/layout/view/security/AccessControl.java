/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.security;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * Reference from a removable UI unit to a {@link SecurityScope}.
 *
 * <p>
 * Authored as an {@code <access-control scope="..."/>} property on a securable element (nav-item,
 * tab, tile). The element is only built when the referenced scope grants access to the current
 * user; otherwise it is omitted entirely. Elements without an {@link AccessControl} are always
 * shown.
 * </p>
 *
 * @see WithAccessControl
 * @see SecurityScopeService
 */
public interface AccessControl extends ConfigurationItem {

	/** Configuration name for {@link #getScope()}. */
	String SCOPE = "scope";

	/**
	 * The id of the {@link SecurityScope} (defined in the {@link SecurityScopeService}) that guards
	 * the enclosing UI unit.
	 */
	@Name(SCOPE)
	@Mandatory
	String getScope();

}
