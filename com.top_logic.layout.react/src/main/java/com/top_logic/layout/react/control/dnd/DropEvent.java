/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.dnd;

import java.util.List;

import com.top_logic.layout.react.control.ReactControl;

/**
 * A drop announced to a {@link DropTarget} for application: the dragged objects and the
 * {@link DropLocation} the accepting operation applies them at.
 *
 * @param source
 *        The control the objects were dragged out of, or {@code null} when the drop was replayed
 *        from a recorded script (which names the dragged objects themselves, not the control they
 *        came from).
 * @param kind
 *        The {@link DragSourceControl#dragKind() kind} of the drag, {@code null} for a drag without
 *        a kind. A replayed drop carries the kind the drag had when it was recorded.
 * @param objects
 *        The dragged business objects, as resolved by the {@link DragSourceControl}. Never empty.
 * @param location
 *        Where the drop is applied, the location the {@link DropTarget#check(DropRequest) check}
 *        accepted the drop at.
 */
public record DropEvent(ReactControl source, String kind, List<?> objects, DropLocation location) {

	/**
	 * The business object of the item the drop is made {@link DropLocation.Onto onto},
	 * {@code null} for a drop at another kind of location.
	 */
	public Object target() {
		return location instanceof DropLocation.Onto onto ? onto.target() : null;
	}

}
