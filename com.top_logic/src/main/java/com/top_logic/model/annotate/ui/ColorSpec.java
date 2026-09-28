/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;

/**
 * Specification of the color a value is displayed with, as a {@link ValueColor role} of the design
 * system.
 *
 * <p>
 * A role adapts to the active theme and mode; how it looks is decided once, in the design system,
 * for every place a value of that role appears. A literal color value or the name of a design
 * token is no valid specification.
 * </p>
 *
 * @implNote {@link ValueColor#of(ColorSpec)} resolves the specification to the role the UI
 *           displays.
 */
public interface ColorSpec extends ConfigurationItem {

	/** Configuration name of {@link #getRole()}. */
	String ROLE = "role";

	/**
	 * The color role to display with: <code>neutral</code>, <code>brand</code>, one of the four
	 * meanings <code>error</code>, <code>warning</code>, <code>success</code>, <code>info</code>,
	 * or one of the eight categories <code>category-1</code> to <code>category-8</code>.
	 *
	 * <p>
	 * A meaning says by its color what the value stands for; a category only tells the value apart
	 * from others and carries no meaning.
	 * </p>
	 */
	@Name(ROLE)
	@Nullable
	@Label("Color role")
	ValueColor getRole();

	/**
	 * @see #getRole()
	 */
	void setRole(ValueColor value);

}
