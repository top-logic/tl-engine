/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.dnd;

import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.dnd.TableDropEvent.Position;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Handler that controls drop operations in a table.
 * 
 * @see TableData#getDropTargets()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TableDropTarget {

	/**
	 * Kind of drop operation possible in a {@link TableDropTarget}.
	 * 
	 * @see TableDropTarget#getDropType()
	 */
	enum DropType {
		/**
		 * A drop is specified as "before", or "after" a {@link TableDropEvent#getRefRow() reference
		 * row}.
		 * 
		 * @see TableDropEvent#getPos()
		 */
		ORDERED {
			@Override
			public boolean accepts(TableDropEvent event) {
				// A drop onto the table itself (no reference row) appends to the end.
				return event.getPos() != Position.ONTO || event.getRefRow() < 0;
			}
		},

		/**
		 * A drop occurs to a row itself, without positioning relative to the target row.
		 */
		ROW {
			@Override
			public boolean accepts(TableDropEvent event) {
				return event.getPos() == Position.ONTO;
			}
		},

		;

		/**
		 * Whether the position of the given event describes a drop of this type.
		 * 
		 * <p>
		 * A table may have {@link TableDropTarget}s of different types. A drop event is only passed
		 * to those targets, whose type accepts the event's position.
		 * </p>
		 */
		public abstract boolean accepts(TableDropEvent event);
	}

	/**
	 * HTML data attribute that transports the {@link #getDropType()}s of all drop targets of a
	 * table to the client.
	 * 
	 * <p>
	 * The value is a space separated list of {@link DropType} names.
	 * </p>
	 */
	String TL_DROPTYPE_ATTR = HTMLConstants.DATA_ATTRIBUTE_PREFIX + "droptype";

	/**
	 * The {@link DropType}, this {@link TableDropTarget} can interpret.
	 * 
	 * @see #TL_DROPTYPE_ATTR
	 */
	default DropType getDropType() {
		return DropType.ORDERED;
	}

	/**
	 * Whether the given table accepts drop operations.
	 * 
	 * @param data
	 *        The potential table drop target.
	 */
	default boolean dropEnabled(TableData data) {
		return true;
	}

	/**
	 * Announces a drop operation on a table.
	 * 
	 * @param event
	 *        Information further describing the drop details.
	 * 
	 * @see TableDropEvent#getTarget()
	 */
	void handleDrop(TableDropEvent event);

	/**
	 * Checking if a drop at the current position can be performed.
	 * 
	 * @param event
	 *        Information further describing the drop details.
	 * 
	 * @see TableDropEvent#getTarget()
	 */
	default boolean canDrop(TableDropEvent event) {
		return true;
	}

}
