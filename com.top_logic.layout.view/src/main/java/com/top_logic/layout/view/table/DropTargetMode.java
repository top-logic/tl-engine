/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.layout.view.dnd.DropBinding;
import com.top_logic.layout.view.dnd.DropSignature;

/**
 * What a declared drop of a table targets: the table as a whole, a single row of it, or a place in
 * the order of its rows.
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
	TABLE("table", DropSignature.CONTROL),

	/**
	 * A single row is the target.
	 *
	 * <p>
	 * A drop applies to the row it was made on, which is what the drop's
	 * {@code target-channel} carries into the action chain; a drop beside the rows applies to
	 * nothing. The rows are highlighted individually while such a drag moves over the table.
	 * </p>
	 */
	ROW("row", DropSignature.ONTO),

	/**
	 * A place between two rows is the target: the dropped objects are inserted there.
	 *
	 * <p>
	 * While such a drag moves over the table, an insertion line is drawn between the rows: a drop in
	 * the upper half of a row inserts before that row, a drop in its lower half before the row
	 * following it, and a drop beside the rows appends at the end. The row the objects are inserted
	 * before is what the drop's {@code before-channel} carries into the action chain, and what its
	 * {@code refuse-if} receives as first argument, {@code before -> objects -> reason}; it is
	 * {@code null} for an insertion at the end. Which order the insertion changes is up to the
	 * action chain - typically the list the {@code rows} of the table are computed from.
	 * </p>
	 *
	 * <p>
	 * Declared next to a {@code row} drop, a row is split into thirds. The declared order decides
	 * which drop applies where: an insertion declared first takes the upper and lower thirds of a
	 * row and leaves the middle to the {@code row} drop, as well as any insertion it refuses; a
	 * {@code row} drop declared first takes the whole row, and leaves the insertion only the place
	 * beside the rows.
	 * </p>
	 */
	ORDERED("ordered", DropSignature.ORDERED_LIST);

	private final String _externalName;

	private final DropSignature _signature;

	private DropTargetMode(String externalName, DropSignature signature) {
		_externalName = externalName;
		_signature = signature;
	}

	/**
	 * The {@link DropSignature} a {@link DropBinding} applies a drop of this target with: on the
	 * table as a whole, onto a row, or as an insertion among the rows.
	 */
	public DropSignature signature() {
		return _signature;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

}
