/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.dnd;

import com.top_logic.layout.dnd.DndData;
import com.top_logic.layout.form.FormMember;

/**
 * {@link FieldDropTarget} that does not accept any drops.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NoFieldDrop implements FieldDropTarget {

	/**
	 * Singleton {@link NoFieldDrop} instance.
	 */
	public static final NoFieldDrop INSTANCE = new NoFieldDrop();

	private NoFieldDrop() {
		// Singleton constructor.
	}

	@Override
	public boolean dropEnabled(FormMember field) {
		return false;
	}

	@Override
	public void handleDrop(FormMember field, DndData data) {
		throw new UnsupportedOperationException();
	}

}
