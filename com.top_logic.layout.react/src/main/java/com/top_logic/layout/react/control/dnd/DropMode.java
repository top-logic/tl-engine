/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.dnd;

/**
 * How a drop operation of a {@link DropTarget} relates a drop to the items of the control it is
 * made on.
 *
 * <p>
 * Each mode has its own kind of {@link DropLocation}: a control resolves the place the pointer
 * points at into the location of a mode, and an operation of that mode receives it.
 * </p>
 */
public enum DropMode {

	/**
	 * A drop on the control as a whole, without a reference item.
	 *
	 * @see DropLocation.Control
	 */
	CONTROL("control"),

	/**
	 * A drop onto a single item of the control.
	 *
	 * @see DropLocation.Onto
	 */
	ONTO("onto"),

	/**
	 * An insertion among the items of the control, at a place in their order.
	 *
	 * @see DropLocation.Insert
	 */
	ORDERED("ordered");

	private final String _wireName;

	private DropMode(String wireName) {
		_wireName = wireName;
	}

	/**
	 * The identifier this mode is transmitted under.
	 *
	 * @see #fromWire(String)
	 */
	public String wireName() {
		return _wireName;
	}

	/**
	 * The mode transmitted under the given {@link #wireName()}, {@code null} if it designates none.
	 */
	public static DropMode fromWire(String wireName) {
		for (DropMode mode : values()) {
			if (mode._wireName.equals(wireName)) {
				return mode;
			}
		}
		return null;
	}

}
