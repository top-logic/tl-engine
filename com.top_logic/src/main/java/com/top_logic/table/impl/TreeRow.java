/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.impl;

import com.top_logic.table.Group;
import com.top_logic.table.Row;
import com.top_logic.table.RowKind;

/**
 * A {@link RowKind#DATA data} {@link Row} for a tree node, carrying its depth and
 * expansion state.
 *
 * @param <R>
 *        The business object type.
 */
public final class TreeRow<R> implements Row<R> {

	private final Object _key;

	private final R _data;

	private final int _depth;

	private final boolean _expandable;

	private final boolean _expanded;

	/**
	 * Creates a {@link TreeRow}.
	 *
	 * @param key
	 *        The stable row key (the tree node).
	 * @param data
	 *        The node's business object.
	 * @param depth
	 *        The node's depth in the tree.
	 * @param expandable
	 *        Whether the node has children.
	 * @param expanded
	 *        Whether the node is currently expanded.
	 */
	public TreeRow(Object key, R data, int depth, boolean expandable, boolean expanded) {
		_key = key;
		_data = data;
		_depth = depth;
		_expandable = expandable;
		_expanded = expanded;
	}

	@Override
	public RowKind kind() {
		return RowKind.DATA;
	}

	@Override
	public Object key() {
		return _key;
	}

	@Override
	public R data() {
		return _data;
	}

	@Override
	public Group<R> group() {
		return null;
	}

	@Override
	public int depth() {
		return _depth;
	}

	@Override
	public boolean expandable() {
		return _expandable;
	}

	@Override
	public boolean expanded() {
		return _expanded;
	}

}
