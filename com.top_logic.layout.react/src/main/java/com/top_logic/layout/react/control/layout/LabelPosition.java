/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

/**
 * Where a form field renders its label relative to the input.
 *
 * <p>
 * Used both at the layout level ({@link ReactFormLayoutControl}, where {@link #AUTO} resolves
 * responsively between {@link #SIDE} and {@link #TOP} by available width) and at the field level
 * ({@link ReactFormFieldChromeControl}, where a {@code null} position inherits from the enclosing
 * layout and {@link #AFTER} trails the input, e.g. for a checkbox).
 * </p>
 */
public enum LabelPosition {

	/** Label beside the input. */
	SIDE("side"),

	/** Label above the input. */
	TOP("top"),

	/** Label after the input (e.g. trailing a checkbox); field level only. */
	AFTER("after"),

	/**
	 * Resolve responsively between {@link #SIDE} and {@link #TOP} from the available width; layout
	 * level only.
	 */
	AUTO("auto");

	private final String _protocolName;

	LabelPosition(String protocolName) {
		_protocolName = protocolName;
	}

	/**
	 * The wire value sent to the client for this position.
	 */
	public String protocolName() {
		return _protocolName;
	}

}
