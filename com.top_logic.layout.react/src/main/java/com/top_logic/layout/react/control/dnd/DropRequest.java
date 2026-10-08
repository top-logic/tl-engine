/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.dnd;

import java.util.List;
import java.util.function.Function;

import com.top_logic.layout.react.control.ReactControl;

/**
 * A drop put to a {@link DropTarget} for its {@link DropTarget#check(DropRequest) check}: the
 * dragged objects, and the {@link DropLocation} each {@link DropMode} would apply them at.
 *
 * <p>
 * The control the drop is made on resolves the place the pointer was at into a location per mode;
 * the target tries its operations and picks the one that accepts the drop at the location of its
 * mode.
 * </p>
 *
 * @param source
 *        The control the objects were dragged out of, or {@code null} when the drop was replayed
 *        from a recorded script.
 * @param kind
 *        The {@link DragSourceControl#dragKind() kind} of the drag, {@code null} for a drag without
 *        a kind.
 * @param objects
 *        The dragged business objects. Never empty.
 * @param locations
 *        The location of the drop for a mode, {@code null} where the place yields none for it.
 *        Must not modify anything.
 */
public record DropRequest(ReactControl source, String kind, List<?> objects,
		Function<DropMode, DropLocation> locations) {

	/**
	 * A request offering exactly the given location, for its mode, and none for any other mode.
	 *
	 * <p>
	 * This is the request of a drop that is already placed: a replayed drop, or a drop whose
	 * {@link DropEvent} is applied.
	 * </p>
	 */
	public static DropRequest at(ReactControl source, String kind, List<?> objects, DropLocation location) {
		return new DropRequest(source, kind, objects, mode -> mode == location.mode() ? location : null);
	}

	/**
	 * The request offering exactly the location of the given event.
	 *
	 * @see #at(ReactControl, String, List, DropLocation)
	 */
	public static DropRequest of(DropEvent event) {
		return at(event.source(), event.kind(), event.objects(), event.location());
	}

	/**
	 * The location of the drop for operations of the given mode, {@code null} if the place the drop
	 * was made at yields none for it.
	 */
	public DropLocation location(DropMode mode) {
		return locations.apply(mode);
	}

	/**
	 * The event announcing this drop for application at the given location.
	 */
	public DropEvent event(DropLocation location) {
		return new DropEvent(source, kind, objects, location);
	}

}
