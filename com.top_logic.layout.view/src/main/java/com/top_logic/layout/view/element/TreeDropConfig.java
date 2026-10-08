/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.view.dnd.DropConfig;

/**
 * Configuration of one {@code <drop>} of a {@link TreeElement}: a drop made on the tree as a
 * whole, onto a single node of it, or at a place among its nodes.
 *
 * <p>
 * The {@link #getTargetChannel() target channel}, the {@link #getTargetExecutability() target
 * executability} rules and the {@link #getRefuseIf() refusal function} get the object of the node
 * dropped onto for a drop onto a {@link TreeDropTargetMode#NODE node}; a drop on the
 * {@link TreeDropTargetMode#TREE tree} has no target node. An {@link TreeDropTargetMode#ORDERED
 * insertion} has the object the dropped objects are inserted under and the one they are inserted
 * before instead, which the {@link #getParentChannel() parent channel}, the
 * {@link #getBeforeChannel() before channel} and the refusal function get.
 * </p>
 */
@TagName(DropConfig.TAG_NAME)
public interface TreeDropConfig extends DropConfig {

	/** Configuration name for {@link #getTarget()}. */
	String TARGET = "target";

	/**
	 * Whether the tree as a whole, a single node, or a place among the nodes is the target of this
	 * drop.
	 */
	@Name(TARGET)
	TreeDropTargetMode getTarget();

}
