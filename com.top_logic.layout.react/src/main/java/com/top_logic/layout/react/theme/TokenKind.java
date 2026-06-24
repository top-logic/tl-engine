/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.theme;

/**
 * The kind of value a design {@link TokenDef token} holds.
 *
 * <p>
 * Used for validation. It does not affect how the token is emitted: every token becomes a CSS
 * custom property regardless of kind.
 * </p>
 */
public enum TokenKind {

	/** A plain string value (the default). */
	STRING,

	/** A CSS color, e.g. {@code #2968C8}. */
	COLOR,

	/** A CSS length, e.g. {@code 0.5rem}. */
	LENGTH,

	/** A unit-less number. */
	NUMBER,

	/** A CSS font-family list. */
	FONT_FAMILY,

	/** A CSS box-shadow value. */
	SHADOW,

	/** A CSS time/duration, e.g. {@code 150ms}. */
	DURATION;

}
