/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

/**
 * The bullet of a paragraph.
 *
 * @param visible
 *        Whether the paragraph has a bullet.
 * @param character
 *        The bullet character, <code>null</code> for the bullet of the placeholder or the default
 *        bullet.
 * @param color
 *        The bullet color, <code>null</code> for the color of the text.
 */
public record BulletSpec(boolean visible, String character, ColorSpec color) {

	/**
	 * No bullet.
	 */
	public static final BulletSpec NONE = new BulletSpec(false, null, null);

	/**
	 * The default bullet.
	 */
	public static final BulletSpec DEFAULT = new BulletSpec(true, null, null);

}
