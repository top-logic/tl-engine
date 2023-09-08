/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree;

/**
 * Observer interface for {@link TreeData}.
 * 
 * @see TreeDataEvent
 * @see TreeDataEventVisitor
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TreeDataListener {

	/**
	 * Handles the change of the internal structure of a {@link TreeData} defined by the given
	 * event.
	 * 
	 * <p>
	 * To handle concrete change, use a {@link TreeDataEventVisitor} to inspect the given event.
	 * </p>
	 * 
	 * @param event
	 *        An event informing about an internal change of the {@link TreeData}.
	 */
	void handleTreeDataChange(TreeDataEvent event);

}
