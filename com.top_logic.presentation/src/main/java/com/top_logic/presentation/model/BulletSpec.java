/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

import org.apache.poi.sl.usermodel.AutoNumberingScheme;

/**
 * The bullet of a paragraph.
 *
 * <p>
 * A bullet is either a character or a number counted automatically by PowerPoint
 * ({@link #autoNumber()}). A numbered paragraph shows the number {@link #start()}, or, without it,
 * the number following the one of the preceding numbered paragraph of the same scheme.
 * </p>
 *
 * @param visible
 *        Whether the paragraph has a bullet.
 * @param character
 *        The bullet character, <code>null</code> for a number, the bullet of the placeholder or
 *        the default bullet.
 * @param color
 *        The bullet color, <code>null</code> for the color of the text.
 * @param autoNumber
 *        The numbering scheme of a numbered paragraph, e.g.
 *        {@link AutoNumberingScheme#arabicPeriod} for "1.", "2.", ...; <code>null</code> for a
 *        bullet character.
 * @param start
 *        The number of a numbered paragraph, <code>null</code> to continue the numbering of the
 *        preceding paragraph, or to start with 1.
 */
public record BulletSpec(boolean visible, String character, ColorSpec color, AutoNumberingScheme autoNumber,
		Integer start) {

	/**
	 * No bullet.
	 */
	public static final BulletSpec NONE = new BulletSpec(false, null, null);

	/**
	 * The default bullet.
	 */
	public static final BulletSpec DEFAULT = new BulletSpec(true, null, null);

	/**
	 * Creates a bullet that is not numbered.
	 */
	public BulletSpec(boolean visible, String character, ColorSpec color) {
		this(visible, character, color, null, null);
	}

	/**
	 * Whether the paragraph shows a number instead of a bullet character.
	 */
	public boolean isNumbered() {
		return visible && autoNumber != null;
	}

}
