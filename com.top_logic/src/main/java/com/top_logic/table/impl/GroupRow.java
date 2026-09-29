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
 * A synthetic {@link RowKind#GROUP_HEADER group header} {@link Row} carrying its
 * materialized {@link Group}. The header doubles as the group's subtotal row: a view
 * renders the group label in the first column and per-column aggregates in the rest.
 *
 * @param <R>
 *        The row business object type.
 */
public final class GroupRow<R> implements Row<R> {

	private final Group<R> _group;

	private final int _depth;

	private final boolean _expanded;

	/**
	 * Creates a {@link GroupRow}.
	 *
	 * @param group
	 *        The materialized group.
	 * @param depth
	 *        The nesting depth.
	 * @param expanded
	 *        Whether the group is currently expanded.
	 */
	public GroupRow(Group<R> group, int depth, boolean expanded) {
		_group = group;
		_depth = depth;
		_expanded = expanded;
	}

	@Override
	public RowKind kind() {
		return RowKind.GROUP_HEADER;
	}

	@Override
	public Object key() {
		return _group.key();
	}

	@Override
	public R data() {
		return null;
	}

	@Override
	public Group<R> group() {
		return _group;
	}

	@Override
	public int depth() {
		return _depth;
	}

	@Override
	public boolean expandable() {
		return true;
	}

	@Override
	public boolean expanded() {
		return _expanded;
	}

}
