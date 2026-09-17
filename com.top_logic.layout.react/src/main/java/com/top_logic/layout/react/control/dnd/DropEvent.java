/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.dnd;

import java.util.List;

import com.top_logic.layout.react.control.ReactControl;

/**
 * A drop announced to a {@link DropTarget}.
 *
 * @param source
 *        The control the objects were dragged out of, or {@code null} when the drop was replayed
 *        from a recorded script (which names the dragged objects themselves, not the control they
 *        came from).
 * @param objects
 *        The dragged business objects, as resolved by the {@link DragSourceControl}. Never empty.
 * @param target
 *        The business object of the row the drop was made on, or {@code null} when the drop was
 *        made on the control as a whole.
 * @param position
 *        Where the drop happened relative to {@code target}; {@link DropPosition#NONE} when there
 *        is no {@code target}.
 */
public record DropEvent(ReactControl source, List<?> objects, Object target, DropPosition position) {
	// Value type.
}
