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
 * <p>
 * The rows of a table are a flat list or - in a tree table - a tree; a place in their order refers
 * to the row inserted before in either, and in a tree to the row inserted under as well.
 * </p>
 *
 * @implNote A flat table applies a drop with the {@link #signature()} of its target, a tree table
 *           with its {@link #treeSignature()}.
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
	TABLE("table", DropSignature.CONTROL, DropSignature.CONTROL),

	/**
	 * A single row is the target.
	 *
	 * <p>
	 * A drop applies to the row it was made on, which is what the drop's
	 * {@code target-channel} carries into the action chain; a drop beside the rows applies to
	 * nothing. The rows are highlighted individually while such a drag moves over the table.
	 * </p>
	 */
	ROW("row", DropSignature.ONTO, DropSignature.ONTO),

	/**
	 * A place between two rows is the target: the dropped objects are inserted there.
	 *
	 * <p>
	 * While such a drag moves over the table, an insertion line is drawn between the rows: a drop in
	 * the upper half of a row inserts before that row, a drop in its lower half before the row
	 * following it, and a drop beside the rows appends at the end. The row the objects are inserted
	 * before is what the drop's {@code before-channel} carries into the action chain, and what its
	 * {@code refuse-if} receives after the dragged objects, {@code objects -> before -> reason}; it is
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
	 *
	 * <p>
	 * In a tree table, the place is one among the children of a row, as in a tree: a row is split
	 * into thirds, a drop in the upper third inserts before the row among its siblings, in the
	 * middle third as the first children of the row, in the lower third as the first children of an
	 * expanded row with children and after the row among its siblings otherwise, and beside the rows
	 * as the last top-level rows. The row the objects are inserted under is what the drop's
	 * {@code parent-channel} carries, the object the top-level rows are the children of for an
	 * insertion among them; {@code refuse-if} receives it before the row to insert before,
	 * {@code objects -> parent -> before -> reason}. An insertion declared first takes the middle
	 * third of a row as an insertion into it, and leaves it to a {@code row} drop only where it
	 * refuses.
	 * </p>
	 */
	ORDERED("ordered", DropSignature.ORDERED_LIST, DropSignature.ORDERED_TREE);

	private final String _externalName;

	private final DropSignature _signature;

	private final DropSignature _treeSignature;

	private DropTargetMode(String externalName, DropSignature signature, DropSignature treeSignature) {
		_externalName = externalName;
		_signature = signature;
		_treeSignature = treeSignature;
	}

	/**
	 * The {@link DropSignature} a {@link DropBinding} applies a drop of this target with in a table
	 * whose rows are a flat list: on the table as a whole, onto a row, or as an insertion among the
	 * rows.
	 */
	public DropSignature signature() {
		return _signature;
	}

	/**
	 * The {@link DropSignature} a {@link DropBinding} applies a drop of this target with in a table
	 * whose rows form a tree: on the table as a whole, onto a row, or as an insertion under a row
	 * among its children.
	 */
	public DropSignature treeSignature() {
		return _treeSignature;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

}
