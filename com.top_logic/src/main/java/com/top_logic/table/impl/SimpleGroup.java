/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.impl;

import java.util.List;

import com.top_logic.table.Group;
import com.top_logic.table.GroupKey;

/**
 * A {@link Group} backed by an eagerly materialized member list.
 *
 * @param <R>
 *        The row business object type.
 */
public final class SimpleGroup<R> implements Group<R> {

	private final GroupKey _key;

	private final List<R> _members;

	/**
	 * Creates a {@link SimpleGroup}.
	 *
	 * @param key
	 *        The group identity.
	 * @param members
	 *        The member business objects (not copied).
	 */
	public SimpleGroup(GroupKey key, List<R> members) {
		_key = key;
		_members = members;
	}

	@Override
	public GroupKey key() {
		return _key;
	}

	@Override
	public int size() {
		return _members.size();
	}

	@Override
	public List<R> members() {
		return _members;
	}

}
