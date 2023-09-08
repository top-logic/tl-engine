/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.style;

/**
 * An underline style.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum Underline {
	/**
	 * Use the default setting.
	 */
	DEFAULT,

	/**
	 * No underline.
	 */
	NONE,

	/**
	 * Single underline.
	 */
	SINGLE,

	/**
	 * Double underline.
	 */
	DOUBLE,

	/**
	 * Single underline in accounting style.
	 */
	SINGLE_ACCOUNTING,

	/**
	 * Double underline in accounting style.
	 */
	DOUBLE_ACCOUNTING;

	/**
	 * Whether this style is not {@link #DEFAULT}.
	 */
	public boolean isDecided() {
		return this != DEFAULT;
	}

	/**
	 * Whether this style {@link #isDecided()} and not {@link #NONE}.
	 */
	public boolean isUnderline() {
		return isDecided() && this != NONE;
	}
}
