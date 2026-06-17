/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.impl;

import com.top_logic.table.GroupKey;
import com.top_logic.table.Row;
import com.top_logic.table.RowKind;

/**
 * A plain {@link RowKind#DATA data} {@link Row} backed by a business object.
 *
 * @param <R>
 *        The row business object type.
 */
public final class DataRow<R> implements Row<R> {

	private final Object _key;

	private final R _data;

	private final int _depth;

	/**
	 * Creates a top-level {@link DataRow}.
	 *
	 * @param key
	 *        The stable row {@link Row#key() key}.
	 * @param data
	 *        The business object.
	 */
	public DataRow(Object key, R data) {
		this(key, data, 0);
	}

	/**
	 * Creates a {@link DataRow} at the given tree/group depth.
	 *
	 * @param key
	 *        The stable row {@link Row#key() key}.
	 * @param data
	 *        The business object.
	 * @param depth
	 *        The nesting depth.
	 */
	public DataRow(Object key, R data, int depth) {
		_key = key;
		_data = data;
		_depth = depth;
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
	public GroupKey group() {
		return null;
	}

	@Override
	public int depth() {
		return _depth;
	}

	@Override
	public boolean expandable() {
		return false;
	}

	@Override
	public boolean expanded() {
		return false;
	}

}
