/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.dnd;

import com.top_logic.layout.react.control.dnd.DropEvent;

/**
 * What a declared drop of a {@link DropBinding} is made on: the control as a whole, or a single
 * item it displays.
 *
 * <p>
 * The element declaring the drop decides which of the two a drop is, and what its items are; the
 * item a drop was made on reaches the binding as the {@link DropEvent#target() target} of the drop.
 * </p>
 */
public enum DropScope {

	/**
	 * The control as a whole is the target: a drop anywhere on it applies, and has no target item.
	 */
	CONTROL,

	/**
	 * A single item of the control is the target: a drop applies to the item it was made on, and a
	 * drop beside the items applies to nothing.
	 */
	ITEM;

}
