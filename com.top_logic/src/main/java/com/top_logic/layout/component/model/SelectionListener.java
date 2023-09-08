/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.model;

import java.util.Set;

import com.top_logic.mig.html.SelectionModel;

/**
 * Listener interfaces that is informed about changes in {@link SelectionModel} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface SelectionListener {

	/**
	 * Notifies the receiver that the selected objects of the given {@link SelectionModel} have
	 * changed.
	 * 
	 * @param model
	 *            the changed {@link SelectionModel}.
	 * @param formerlySelectedObjects
	 *            the set of objects which were formerly selected. never <code>null</code>.
	 * @param selectedObjects
	 *            the set of objects which are now selected. never <code>null</code>.
	 */
	void notifySelectionChanged(SelectionModel model, Set<?> formerlySelectedObjects, Set<?> selectedObjects);

}