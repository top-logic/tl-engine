/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

/**
 * The alignment of the children of a {@link ContainerSpec} across its direction: vertically in a
 * row, horizontally in a column.
 */
public enum CrossAlign implements DataEnum {

	/** A child without own size across the direction takes the whole size of the container. */
	STRETCH("stretch"),

	/** The children are aligned to the top (in a row) or left (in a column) border. */
	START("start"),

	/** The children are centered. */
	CENTER("center"),

	/** The children are aligned to the bottom (in a row) or right (in a column) border. */
	END("end");

	private final String _id;

	CrossAlign(String id) {
		_id = id;
	}

	@Override
	public String id() {
		return _id;
	}

}
