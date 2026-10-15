/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

/**
 * The handling of a text that does not fit into its element.
 *
 * <p>
 * A text fits, if its height does not exceed the height of the element minus the inner margins
 * and no word has to be broken within to fit into the width. The text of a table cell fits, if no
 * word is broken, since the rows grow with their text.
 * </p>
 */
public enum Overflow implements DataEnum {

	/**
	 * All font sizes of the text are reduced in the same proportion until the text fits, but not
	 * below the smallest font size of the template profile. If the text does not fit at that size,
	 * it is cut off as with {@link #ELLIPSIS}.
	 */
	SHRINK("shrink"),

	/**
	 * The text keeps its size and is cut off after the last word that fits, followed by "…".
	 */
	ELLIPSIS("ellipsis"),

	/** The text is left as it is and may exceed its element. */
	VISIBLE("visible"),

	/** A text that does not fit is an error. */
	ERROR("error");

	private final String _id;

	Overflow(String id) {
		_id = id;
	}

	@Override
	public String id() {
		return _id;
	}

}
