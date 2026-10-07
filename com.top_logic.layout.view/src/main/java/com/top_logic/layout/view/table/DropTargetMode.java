/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.layout.view.dnd.DropBinding;
import com.top_logic.layout.view.dnd.DropScope;

/**
 * What a declared drop of a table targets: the table as a whole, or a single row of it.
 *
 * @see TableDropConfig
 * @see DropBinding
 */
public enum DropTargetMode implements ExternallyNamed {

	/**
	 * The table as a whole is the target.
	 *
	 * <p>
	 * A drop anywhere on the table applies, and the action chain is told nothing beyond the dropped
	 * objects - what the drop means is the same wherever it was made.
	 * </p>
	 */
	TABLE("table", DropScope.CONTROL),

	/**
	 * A single row is the target.
	 *
	 * <p>
	 * A drop applies to the row it was made on, which is what the drop's
	 * {@code target-channel} carries into the action chain; a drop beside the rows applies to
	 * nothing. The rows are highlighted individually while such a drag moves over the table.
	 * </p>
	 */
	ROW("row", DropScope.ITEM);

	private final String _externalName;

	private final DropScope _scope;

	private DropTargetMode(String externalName, DropScope scope) {
		_externalName = externalName;
		_scope = scope;
	}

	/**
	 * What a {@link DropBinding} makes a drop of this mode on: the table as a whole is the control,
	 * a row is an item of it.
	 */
	public DropScope scope() {
		return _scope;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

}
