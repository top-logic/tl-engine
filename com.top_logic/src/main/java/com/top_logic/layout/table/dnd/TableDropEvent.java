/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.dnd;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.dnd.DndData;
import com.top_logic.layout.dnd.DropEvent;
import com.top_logic.layout.table.TableData;

/**
 * Event data announcing a drop operation to a {@link TableDropTarget}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TableDropEvent extends DropEvent {

	/**
	 * Specifier where a drop event happened relative to a reference row in a table.
	 */
	public enum Position {

		/**
		 * Just before the reference row.
		 */
		ABOVE,

		/**
		 * Just after the reference row.
		 */
		BELOW,

		/**
		 * Unordered drop on the reference row.
		 */
		ONTO;

		/**
		 * Parses a client-side value to a {@link Position} constant.
		 */
		public static Position fromString(String pos) {
			if (StringServices.isEmpty(pos)) {
				// Legacy compatibility.
				return ONTO;
			}
			switch (pos) {
				case "above":
					return ABOVE;
				case "below":
					return BELOW;
				case "onto":
					return ONTO;
			}
			throw new IllegalArgumentException("No such position: " + pos);
		}
	}

	private TableData _target;

	private int _refRow;

	private Position _pos;

	/**
	 * Creates a {@link TableDropEvent}.
	 *
	 * @param data
	 *        The dragged data.
	 * @param target
	 *        See {@link #getTarget()}.
	 * @param row
	 *        See {@link #getRefRow()}.
	 * @param pos
	 *        See {@link #getPos()}.
	 */
	public TableDropEvent(DndData data, TableData target, int row, Position pos) {
		super(data);

		_target = target;
		_refRow = row;
		_pos = pos;
	}

	/**
	 * The target {@link TableData} that received the drop.
	 */
	@Override
	public TableData getTarget() {
		return _target;
	}

	/**
	 * The reference row near which the drop happened.
	 * 
	 * @see #getPos()
	 */
	public int getRefRow() {
		return _refRow;
	}

	/**
	 * {@link Position} relative to the {@link #getRefRow()} where the drop happened.
	 * 
	 * @see #getRefRow()
	 */
	public Position getPos() {
		return _pos;
	}

}
