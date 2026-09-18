/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.gui;

/**
 * The kind of value a design token holds.
 *
 * <p>
 * A configuration naming a design token expects a value of a certain kind — a color annotation
 * accepts a color token and nothing else. The kind therefore selects the part of the token
 * vocabulary such a configuration may choose from.
 * </p>
 *
 * @see DesignTokenService
 */
public enum DesignTokenKind {

	/**
	 * A color, such as the background of a status pill.
	 */
	COLOR,

	/**
	 * A length, such as a spacing, a border width, or a font size.
	 */
	LENGTH,

	/**
	 * A number without a unit, such as a line height or an opacity.
	 */
	NUMBER,

	/**
	 * A text value, such as a font family, a shadow, or a transition.
	 */
	TEXT;

}
