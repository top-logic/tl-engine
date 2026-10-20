/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

/**
 * The direction of a block arrow.
 */
public enum ArrowDirection implements DataEnum {

	/** Pointing to the right. */
	RIGHT("right", "rightArrow", true),

	/** Pointing to the left. */
	LEFT("left", "leftArrow", true),

	/** Pointing upwards. */
	UP("up", "upArrow", false),

	/** Pointing downwards. */
	DOWN("down", "downArrow", false);

	private final String _id;

	private final String _preset;

	private final boolean _horizontal;

	ArrowDirection(String id, String preset, boolean horizontal) {
		_id = id;
		_preset = preset;
		_horizontal = horizontal;
	}

	@Override
	public String id() {
		return _id;
	}

	/**
	 * The name of the preset geometry of an arrow in this direction.
	 */
	public String preset() {
		return _preset;
	}

	/**
	 * Whether the arrow points to the left or right.
	 */
	public boolean isHorizontal() {
		return _horizontal;
	}

}
