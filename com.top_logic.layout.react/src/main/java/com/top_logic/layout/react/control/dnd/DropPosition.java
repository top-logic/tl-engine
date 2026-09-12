/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.dnd;

/**
 * Where a drop happened relative to the {@link DropEvent#target() target} it was made on.
 *
 * <p>
 * A drop made on the container itself rather than on one of its elements has no reference element
 * and is reported as {@link #NONE}. A drop on an element is either an unordered drop
 * {@link #ONTO} it, or an insertion {@link #BEFORE} or {@link #AFTER} it.
 * </p>
 */
public enum DropPosition {

	/** Insert just before the {@link DropEvent#target() target}. */
	BEFORE("before"),

	/** Insert just after the {@link DropEvent#target() target}. */
	AFTER("after"),

	/** Unordered drop on the {@link DropEvent#target() target} itself. */
	ONTO("onto"),

	/** Drop on the container, without a reference element. */
	NONE("none");

	private final String _wireName;

	private DropPosition(String wireName) {
		_wireName = wireName;
	}

	/**
	 * The identifier this position is transmitted under.
	 *
	 * @see #fromWire(String)
	 */
	public String wireName() {
		return _wireName;
	}

	/**
	 * The position transmitted under the given {@link #wireName()}.
	 *
	 * @param wireName
	 *        The transmitted identifier. An absent or empty value designates {@link #NONE}: a drop
	 *        on the container names no reference element and need not name a position either.
	 * @return The designated position, or {@code null} if {@code wireName} designates none.
	 */
	public static DropPosition fromWire(String wireName) {
		if (wireName == null || wireName.isEmpty()) {
			return NONE;
		}
		for (DropPosition position : values()) {
			if (position._wireName.equals(wireName)) {
				return position;
			}
		}
		return null;
	}

}
