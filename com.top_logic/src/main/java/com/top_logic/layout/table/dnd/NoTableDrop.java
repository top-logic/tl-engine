/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.dnd;

import com.top_logic.layout.table.TableData;

/**
 * {@link TableDropTarget} that prevents all drops.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NoTableDrop implements TableDropTarget {

	/**
	 * Singleton {@link NoTableDrop} instance.
	 */
	public static final NoTableDrop INSTANCE = new NoTableDrop();

	private NoTableDrop() {
		// Singleton constructor.
	}

	@Override
	public boolean dropEnabled(TableData data) {
		return false;
	}

	@Override
	public void handleDrop(TableDropEvent event) {
		// Ignore.
	}

}
