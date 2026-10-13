/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.dnd;

/**
 * The place within a control a drop was made at - an item and a {@link DropZone zone} of it, or
 * beside the items - as the control displaying the items understands it.
 *
 * <p>
 * A control accepting drops resolves the client-side key and zone a drop names into a place (see
 * {@link Resolver}); the place yields the {@link DropLocation} of each {@link DropMode} and the
 * {@link DropMarker marker} the client draws for the location a drop is accepted at.
 * {@link DropSupport} does everything else.
 * </p>
 */
public interface DropPlace {

	/**
	 * The location of a drop of the given mode made at this place, {@code null} if the place
	 * yields none for the mode.
	 *
	 * <p>
	 * Must not modify anything.
	 * </p>
	 */
	DropLocation location(DropMode mode);

	/**
	 * The marker the client draws for a drop accepted here at the given location.
	 *
	 * @param location
	 *        A location this place yielded.
	 */
	DropMarker marker(DropLocation location);

	/**
	 * The client-side key of the item the given marker is drawn at, {@code null} for a marker of
	 * the control as a whole.
	 *
	 * @param marker
	 *        A marker this place answered.
	 */
	String markerKey(DropMarker marker);

	/**
	 * Resolves the item a drop names into the place the drop was made at.
	 */
	@FunctionalInterface
	interface Resolver {

		/**
		 * The place of the given item and zone.
		 *
		 * @param itemKey
		 *        The client-side key of the item the drop was made on, {@code null} for a drop
		 *        beside the items.
		 * @param zone
		 *        The zone of the item the drop was made in, {@link DropZone#NONE} without an item.
		 * @return The place, {@code null} if {@code itemKey} designates no item the control
		 *         displays.
		 */
		DropPlace place(String itemKey, DropZone zone);

	}

}
