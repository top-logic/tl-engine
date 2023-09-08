/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.dnd;

import com.top_logic.layout.dnd.DndData;
import com.top_logic.layout.form.FormMember;

/**
 * Handler for accepting drop operations in {@link FormMember}s.
 * 
 * @see FieldDrop#setDropTarget(FormMember, FieldDropTarget) Setting a custom drop handler.
 * @see DefaultSelectFieldDrop
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FieldDropTarget {

	/**
	 * Whether drop should be enabled for the given field.
	 */
	boolean dropEnabled(FormMember field);

	/**
	 * Called, when a drop operation happens in the UI.
	 * 
	 * @param field
	 *        The target of the drop.
	 * @param data
	 *        The dropped data.
	 */
	void handleDrop(FormMember field, DndData data);

}
