/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.dnd;

import com.top_logic.layout.table.TableData;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Handler that controls drop operations in a table.
 * 
 * @see TableData#getDropTarget()
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
		ORDERED,

		/**
		 * A drop occurs to a row itself, without positioning relative to the target row.
		 */
		ROW,

		;
	}

	/**
	 * HTML data attribute that transports the {@link #getDropType()} to the client.
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
