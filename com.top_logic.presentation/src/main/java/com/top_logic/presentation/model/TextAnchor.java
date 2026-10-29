/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

/**
 * Vertical alignment of a text in its shape.
 */
public enum TextAnchor implements DataEnum {

	/** At the top border. */
	TOP("top"),

	/** Centered vertically. */
	MIDDLE("middle"),

	/** At the bottom border. */
	BOTTOM("bottom");

	private final String _id;

	TextAnchor(String id) {
		_id = id;
	}

	@Override
	public String id() {
		return _id;
	}

}
