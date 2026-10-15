/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

/**
 * Horizontal alignment of a paragraph.
 */
public enum TextAlign implements DataEnum {

	/** Aligned to the left border. */
	LEFT("left"),

	/** Centered. */
	CENTER("center"),

	/** Aligned to the right border. */
	RIGHT("right"),

	/** Aligned to both borders. */
	JUSTIFY("justify");

	private final String _id;

	TextAlign(String id) {
		_id = id;
	}

	@Override
	public String id() {
		return _id;
	}

}
