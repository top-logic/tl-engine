/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;

/**
 * Configuration mixin adding an optional {@link VisibilityCondition} to a UI unit that its
 * container shows as an entry of its own (tab, accordion section).
 *
 * <p>
 * The condition decides by the objects the unit is displayed for whether the unit belongs to its
 * container at all, e.g. a tab only offered for objects of a certain type. It is no access check:
 * a unit whose condition does not hold is left out of its container silently, and its
 * {@link com.top_logic.layout.view.security.WithAccessControl access control} is not consulted
 * meanwhile.
 * </p>
 *
 * @see UnitVisibility
 */
public interface WithVisibility extends ConfigurationItem {

	/** Configuration name for {@link #getVisibility()}. */
	String VISIBILITY = "visibility";

	/**
	 * The condition under which this UI unit is shown in its container.
	 *
	 * @return {@code null} when the unit is shown independent of the displayed objects.
	 * @see VisibilityCondition
	 */
	@Name(VISIBILITY)
	@Nullable
	VisibilityCondition getVisibility();

}
