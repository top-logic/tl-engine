/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.dnd;

import com.top_logic.layout.tree.TreeData;

/**
 * {@link TreeDropTarget} preventing dropping anything over a tree.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NoTreeDrop implements TreeDropTarget {

	/**
	 * Singleton {@link NoTreeDrop} instance.
	 */
	public static final NoTreeDrop INSTANCE = new NoTreeDrop();

	private NoTreeDrop() {
		// Singleton constructor.
	}

	@Override
	public boolean dropEnabled(TreeData model) {
		return false;
	}

	@Override
	public void handleDrop(TreeDropEvent event) {
		// Ignore.
	}

}
