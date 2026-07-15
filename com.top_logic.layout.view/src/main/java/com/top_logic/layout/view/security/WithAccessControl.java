/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.security;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;

/**
 * Configuration mixin adding an optional {@link AccessControl} reference to a removable UI unit.
 *
 * <p>
 * Securable element configurations (nav-item, tab, tile) extend this interface so that the same
 * {@code <access-control>} declaration and the same enforcement code ({@link AccessChecks}) apply
 * uniformly across all of them.
 * </p>
 *
 * @see AccessChecks#isAccessible(WithAccessControl)
 */
public interface WithAccessControl extends ConfigurationItem {

	/** Configuration name for {@link #getAccessControl()}. */
	String ACCESS_CONTROL = "access-control";

	/**
	 * The access control guarding this UI unit.
	 *
	 * @return {@code null} when the unit is not access controlled (always shown).
	 */
	@Name(ACCESS_CONTROL)
	@Nullable
	AccessControl getAccessControl();

}
