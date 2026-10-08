/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.dnd;

/**
 * Where a drop operation of a {@link DropMode} applies a drop: on the control as a whole, onto an
 * item, or at a place in the order of the items.
 *
 * <p>
 * A control resolves the item and the {@link DropZone} a drop was made in into the location of
 * each mode; a mode for which the place yields no location does not apply there.
 * </p>
 */
public sealed interface DropLocation {

	/**
	 * The mode of the operations this location is meant for.
	 */
	DropMode mode();

	/**
	 * The location of a {@link DropMode#CONTROL} drop: the control as a whole.
	 */
	record Control() implements DropLocation {

		@Override
		public DropMode mode() {
			return DropMode.CONTROL;
		}

	}

	/**
	 * The location of a {@link DropMode#ONTO} drop: a single item.
	 *
	 * @param target
	 *        The business object of the item the drop is made onto.
	 */
	record Onto(Object target) implements DropLocation {

		@Override
		public DropMode mode() {
			return DropMode.ONTO;
		}

	}

	/**
	 * The location of a {@link DropMode#ORDERED} drop: a place in the order of the items.
	 *
	 * @param parent
	 *        The business object whose children the dropped objects are inserted among,
	 *        {@code null} for the items of a flat list.
	 * @param before
	 *        The business object of the item the dropped objects are inserted before, {@code null}
	 *        for an insertion at the end.
	 */
	record Insert(Object parent, Object before) implements DropLocation {

		@Override
		public DropMode mode() {
			return DropMode.ORDERED;
		}

	}

}
