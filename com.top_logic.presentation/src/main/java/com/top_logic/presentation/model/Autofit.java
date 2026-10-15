/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

/**
 * How a text is fitted into its shape.
 */
public enum Autofit implements DataEnum {

	/** The text keeps its size and may overflow the shape. */
	NONE("none"),

	/** The text is shrunk until it fits into the shape. */
	SHRINK("shrink");

	private final String _id;

	Autofit(String id) {
		_id = id;
	}

	@Override
	public String id() {
		return _id;
	}

}
