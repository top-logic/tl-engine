/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.layout.view.dnd.DragConfig;

/**
 * Configuration of the {@code <drag>} of a {@link TreeElement}: that its nodes may be dragged, the
 * {@link #getKind() kind} of such a drag, and when they may be dragged.
 *
 * <p>
 * The {@link #getExecutability() executability} rules decide for the tree as a whole over the
 * value of the {@link #getInput() input} channel, and are followed live: while they refuse, no node
 * can be dragged. The {@link #getNodeExecutability() node executability} rules decide for each node
 * separately, with the object of the node as their input.
 * </p>
 */
public interface TreeDragConfig extends DragConfig {

	/** Configuration name for {@link #getNodeExecutability()}. */
	String NODE_EXECUTABILITY = "node-executability";

	/**
	 * Rules deciding which nodes may be dragged, the object of each node being the input they
	 * decide over.
	 *
	 * <p>
	 * A node the rules refuse offers no drag, and a drag of a selection including such a node is
	 * refused as a whole. Empty (default) lets every node be dragged while the tree-wide
	 * {@link #getExecutability() executability} allows dragging at all.
	 * </p>
	 */
	@Name(NODE_EXECUTABILITY)
	@EntryTag(RULE)
	List<PolymorphicConfiguration<? extends ViewExecutabilityRule>> getNodeExecutability();

}
