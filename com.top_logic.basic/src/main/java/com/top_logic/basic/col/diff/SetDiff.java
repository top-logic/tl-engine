/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.diff;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.col.diff.op.Update;

/**
 * Algorithm analyzing sets of items for differences.
 * 
 * @see CollectionDiff#diffSet(java.util.function.Function, Collection, Collection)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SetDiff<T> {

	private final List<T> _created;

	private final List<Update<T>> _updated;

	private final List<T> _deleted;

	/**
	 * Creates a {@link SetDiff}.
	 *
	 * @param created
	 *        See {@link #getCreated()}.
	 * @param updated
	 *        See {@link #getUpdated()}.
	 * @param deleted
	 *        See {@link #getDeleted()}.
	 */
	SetDiff(List<T> created, List<Update<T>> updated, List<T> deleted) {
		_created = created;
		_updated = updated;
		_deleted = deleted;
	}

	/**
	 * The newly created items.
	 */
	public List<T> getCreated() {
		return _created;
	}

	/**
	 * The deleted items.
	 */
	public List<T> getDeleted() {
		return _deleted;
	}

	/**
	 * The items that are potentially updated internally.
	 */
	public List<Update<T>> getUpdated() {
		return _updated;
	}

}