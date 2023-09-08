/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.dnd;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.dnd.DndData;
import com.top_logic.layout.dnd.DropEvent;
import com.top_logic.layout.tree.TreeData;

/**
 * Event data announcing a drop operation in a {@link TreeDropTarget}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TreeDropEvent extends DropEvent {

	/**
	 * Specifier where a drop event happened relative to a reference node in a tree.
	 */
	public enum Position {

		/**
		 * Just above the reference node within its siblings.
		 */
		ABOVE,

		/**
		 * Before the first child of a reference node (even if the reference node has no children
		 * which means that the drop should insert a first child).
		 */
		WITHIN,

		/**
		 * Just after the reference node within its siblings.
		 */
		BELOW;

		/**
		 * Parses a client-side value to a {@link Position} constant.
		 */
		public static Position fromString(String pos) {
			if (StringServices.isEmpty(pos)) {
				return null;
			}
			switch (pos) {
				case "above":
					return ABOVE;
				case "within":
					return WITHIN;
				case "below":
					return BELOW;
			}
			throw new IllegalArgumentException("No such position: " + pos);
		}
	}

	private TreeData _target;

	private Object _refNode;

	private Position _pos;

	/**
	 * Creates a {@link TreeDropEvent}.
	 *
	 * @param data
	 *        The dragged data.
	 * @param target
	 *        See {@link #getTarget()}.
	 * @param refNode
	 *        See {@link #getRefNode()}.
	 * @param pos
	 *        See {@link #getPos()}.
	 */
	public TreeDropEvent(DndData data, TreeData target, Object refNode, Position pos) {
		super(data);

		_target = target;
		_refNode = refNode;
		_pos = pos;
	}

	/**
	 * The target {@link TreeData} that received the drop.
	 */
	@Override
	public TreeData getTarget() {
		return _target;
	}

	/**
	 * The reference node near which the drop happened.
	 * 
	 * @see #getPos()
	 */
	public Object getRefNode() {
		return _refNode;
	}

	/**
	 * {@link Position} relative to the {@link #getRefNode()} where the drop happened.
	 * 
	 * @see #getRefNode()
	 */
	public Position getPos() {
		return _pos;
	}

}
