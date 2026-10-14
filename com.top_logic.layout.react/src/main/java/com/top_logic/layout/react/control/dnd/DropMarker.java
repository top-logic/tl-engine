/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.dnd;

/**
 * What a client draws while a drag hovers a place where a drop is accepted: the feedback for the
 * {@link DropLocation} the accepting operation would receive.
 *
 * <p>
 * The marker is answered with the verdict of a drop probe (see {@link DropSupport#VERDICT_MARKER}),
 * together with the item it is drawn at (see {@link DropSupport#VERDICT_MARKER_KEY}).
 * </p>
 */
public enum DropMarker {

	/** An insertion line above the item. */
	BEFORE("before"),

	/** An insertion line below the item. */
	AFTER("after"),

	/** A highlight of the item itself. */
	INTO("into"),

	/** A highlight of the control as a whole. */
	CONTROL("control");

	private final String _wireName;

	private DropMarker(String wireName) {
		_wireName = wireName;
	}

	/**
	 * The identifier this marker is transmitted under.
	 */
	public String wireName() {
		return _wireName;
	}

}
