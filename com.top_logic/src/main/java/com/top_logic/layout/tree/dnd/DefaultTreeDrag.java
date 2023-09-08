/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.dnd;

import com.top_logic.layout.tree.TreeData;

/**
 * {@link TreeDragSource} enabling drag of all nodes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultTreeDrag implements TreeDragSource {

	/**
	 * Singleton {@link DefaultTreeDrag} instance.
	 */
	public static final DefaultTreeDrag INSTANCE = new DefaultTreeDrag();

	/**
	 * Creates a {@link DefaultTreeDrag}.
	 */
	protected DefaultTreeDrag() {
		// Singleton constructor.
	}

	@Override
	public boolean dragEnabled(TreeData data) {
		return true;
	}

	@Override
	public boolean canDrag(TreeData data, Object node) {
		return true;
	}

}
