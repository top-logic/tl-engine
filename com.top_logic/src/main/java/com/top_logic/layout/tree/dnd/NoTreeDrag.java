/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.dnd;

import com.top_logic.layout.tree.TreeData;

/**
 * {@link TreeDragSource} disables drag of all nodes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NoTreeDrag implements TreeDragSource {

	/**
	 * Singleton {@link NoTreeDrag} instance.
	 */
	public static final NoTreeDrag INSTANCE = new NoTreeDrag();

	/**
	 * Creates a {@link NoTreeDrag}.
	 */
	protected NoTreeDrag() {
		// Singleton constructor.
	}

	@Override
	public boolean dragEnabled(TreeData data) {
		return false;
	}

	@Override
	public boolean canDrag(TreeData data, Object node) {
		return false;
	}

}
