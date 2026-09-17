/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.dnd;

import java.util.List;

import com.top_logic.layout.react.control.ReactControl;

/**
 * A {@link ReactControl} whose rows can be dragged out of it.
 *
 * <p>
 * A drag payload carries the id of the control the drag started in plus the client-side keys of the
 * dragged rows — never the objects themselves. The control the drag started in is therefore the
 * only one that can say what was dragged, and it says it here: the receiving {@link DropTarget}
 * gets business objects, and the wire stays free of anything a client could forge into a different
 * object.
 * </p>
 *
 * @see DropTarget
 */
public interface DragSourceControl {

	/**
	 * The type tag of the objects dragged out of this control, or {@code null} while dragging is
	 * switched off.
	 *
	 * <p>
	 * The tag classifies what a drag carries, and a {@link DropTarget} accepts a drop by it (see
	 * {@link DropTarget#acceptedTypes()}). It is an application-level identifier — typically a model
	 * type name — and is compared literally, so a source and a target must agree on its spelling.
	 * </p>
	 */
	String dragType();

	/**
	 * The objects the given row keys designate.
	 *
	 * @param keys
	 *        The client-side row keys of the drag payload.
	 * @return The designated business objects, in the order the keys were given. A key designating
	 *         no row contributes nothing, so the result is shorter than {@code keys} when the
	 *         control has moved on since the drag started.
	 */
	List<?> dragObjects(List<String> keys);

	/**
	 * The objects of this control's current selection.
	 *
	 * <p>
	 * Dragging a selected row drags the whole selection. The selection is read here rather than
	 * taken from the payload: a virtualized control renders only a window of its rows, so the client
	 * cannot enumerate a selection reaching beyond it.
	 * </p>
	 *
	 * @return The selected business objects, in display order.
	 */
	List<?> dragSelection();

}
