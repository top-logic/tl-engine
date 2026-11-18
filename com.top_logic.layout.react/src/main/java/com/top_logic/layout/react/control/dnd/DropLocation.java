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
	 * The point the drop is made at, in the coordinate system of the location, {@code null} if the
	 * control reports none.
	 *
	 * <p>
	 * Only a {@link Control} or {@link Onto} location of a control displaying its items on a surface
	 * has a point. An {@link Insert insertion} has none.
	 * </p>
	 */
	default DropPoint point() {
		return null;
	}

	/**
	 * This location at the given point.
	 *
	 * @param point
	 *        The point the drop is made at, in the coordinate system of this location, {@code null}
	 *        for none.
	 * @return The location of this one's mode and objects at the given point, this location itself
	 *         if it has no point.
	 */
	default DropLocation at(DropPoint point) {
		return this;
	}

	/**
	 * The location of a {@link DropMode#CONTROL} drop: the control as a whole.
	 *
	 * @param point
	 *        The point on the control's surface the drop is made at, relative to its origin,
	 *        {@code null} if the control reports none.
	 */
	record Control(DropPoint point) implements DropLocation {

		/**
		 * The location of a drop on the control as a whole, at no particular point.
		 */
		public Control() {
			this(null);
		}

		@Override
		public DropMode mode() {
			return DropMode.CONTROL;
		}

		@Override
		public DropLocation at(DropPoint newPoint) {
			return new Control(newPoint);
		}

	}

	/**
	 * The location of a {@link DropMode#ONTO} drop: a single item.
	 *
	 * @param target
	 *        The business object of the item the drop is made onto.
	 * @param point
	 *        The point the drop is made at, relative to the upper left corner of the item,
	 *        {@code null} if the control reports none.
	 */
	record Onto(Object target, DropPoint point) implements DropLocation {

		/**
		 * The location of a drop onto the given item, at no particular point of it.
		 */
		public Onto(Object target) {
			this(target, null);
		}

		@Override
		public DropMode mode() {
			return DropMode.ONTO;
		}

		@Override
		public DropLocation at(DropPoint newPoint) {
			return new Onto(target, newPoint);
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
