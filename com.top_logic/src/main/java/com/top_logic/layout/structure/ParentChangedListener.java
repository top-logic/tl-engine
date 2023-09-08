/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.basic.listener.PropertyListener;

/**
 * {@link PropertyListener} that handles change of the parent of an {@link AbstractLayoutControl}.
 * 
 * @see AbstractLayoutControl#PARENT_PROPERTY
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ParentChangedListener extends PropertyListener {

	/**
	 * Handles change of the parent of the given {@link AbstractLayoutControl}.
	 * 
	 * @param sender
	 *        {@link AbstractLayoutControl} whose parent changed.
	 * @param oldParent
	 *        Former parent.
	 * @param newParent
	 *        New parent.
	 */
	void handleParentChanged(LayoutControl sender, LayoutControl oldParent, LayoutControl newParent);

}

