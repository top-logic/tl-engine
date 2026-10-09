/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.layout.view.dnd.DropBinding;
import com.top_logic.layout.view.dnd.DropSignature;

/**
 * What a declared drop of a tree targets: the tree as a whole, a single node of it, or a place
 * among its nodes.
 *
 * @see TreeDropConfig
 * @see DropBinding
 */
public enum TreeDropTargetMode implements ExternallyNamed {

	/**
	 * The tree as a whole is the target.
	 *
	 * <p>
	 * A drop anywhere on the tree applies, and the action chain is told nothing beyond the dropped
	 * objects - what the drop means is the same wherever it was made.
	 * </p>
	 */
	TREE("tree", DropSignature.CONTROL),

	/**
	 * A single node is the target.
	 *
	 * <p>
	 * A drop applies to the object of the node it was made on, which is what the drop's
	 * {@code target-channel} carries into the action chain; a drop beside the nodes applies to
	 * nothing. The nodes are highlighted individually while such a drag moves over the tree.
	 * </p>
	 */
	NODE("node", DropSignature.ONTO),

	/**
	 * A place among the nodes is the target: the dropped objects are inserted there, into the
	 * children of a parent object before one of them.
	 *
	 * <p>
	 * A node is split into thirds while such a drag moves over the tree. A drop in the upper third
	 * of a node inserts before that node among its siblings; in the middle third as the first
	 * children of the node; in the lower third as the first children of an expanded node with
	 * children, and after the node among its siblings otherwise. A drop beside the nodes appends to
	 * the top-level nodes. An insertion line before or after the node, or a highlight of the node the
	 * objects are inserted into, shows the place.
	 * </p>
	 *
	 * <p>
	 * The object whose children the dropped objects become is what the drop's
	 * {@code parent-channel} carries into the action chain, the child they are inserted before is
	 * what its {@code before-channel} carries; its {@code refuse-if} receives both after the dragged
	 * objects, {@code objects -> parent -> before -> reason}. The parent of the top-level nodes is
	 * the object the tree is built from ({@code null} where that object is displayed as a node
	 * itself), and {@code before} is {@code null} for an insertion as the last children. Which
	 * structure the insertion changes is up to the action chain - typically the list the
	 * {@code children} function of the tree reads.
	 * </p>
	 *
	 * <p>
	 * Declared next to a {@code node} drop, the declared order decides which drop applies in the
	 * middle third of a node: an insertion declared first inserts into the node and leaves the node
	 * to the {@code node} drop only where it refuses; a {@code node} drop declared first takes the
	 * whole node, and leaves the insertion only the place beside the nodes.
	 * </p>
	 */
	ORDERED("ordered", DropSignature.ORDERED_TREE);

	private final String _externalName;

	private final DropSignature _signature;

	private TreeDropTargetMode(String externalName, DropSignature signature) {
		_externalName = externalName;
		_signature = signature;
	}

	/**
	 * The {@link DropSignature} a {@link DropBinding} applies a drop of this target with: on the
	 * tree as a whole, onto a node, or as an insertion among the nodes.
	 */
	public DropSignature signature() {
		return _signature;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

}
