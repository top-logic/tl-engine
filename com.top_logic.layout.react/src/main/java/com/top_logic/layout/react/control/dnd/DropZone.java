/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.dnd;

/**
 * Where within an item the pointer was when a drag hovered it or was released over it.
 *
 * <p>
 * A zone is what the client reports; it decides nothing. How an item is split into zones depends on
 * the {@link DropTarget#dropModes() modes} the target announces, and the control resolves the item
 * and the zone into the {@link DropLocation} of each mode. A pointer beside the items - in the empty
 * area of the control - is in no item at all, which is the zone {@link #NONE}.
 * </p>
 */
public enum DropZone {

	/** The upper part of the item. */
	UPPER("upper"),

	/** The middle part of the item, or the whole item where it is not split. */
	MIDDLE("middle"),

	/** The lower part of the item. */
	LOWER("lower"),

	/** Beside the items: the pointer is over the control, but over none of its items. */
	NONE("none");

	private final String _wireName;

	private DropZone(String wireName) {
		_wireName = wireName;
	}

	/**
	 * The identifier this zone is transmitted under.
	 *
	 * @see #fromWire(String)
	 */
	public String wireName() {
		return _wireName;
	}

	/**
	 * The zone transmitted under the given {@link #wireName()}.
	 *
	 * @param wireName
	 *        The transmitted identifier. An absent or empty value designates {@link #NONE}: a drop
	 *        beside the items names no item and need not name a zone either.
	 * @return The designated zone, or {@code null} if {@code wireName} designates none.
	 */
	public static DropZone fromWire(String wireName) {
		if (wireName == null || wireName.isEmpty()) {
			return NONE;
		}
		for (DropZone zone : values()) {
			if (zone._wireName.equals(wireName)) {
				return zone;
			}
		}
		return null;
	}

}
