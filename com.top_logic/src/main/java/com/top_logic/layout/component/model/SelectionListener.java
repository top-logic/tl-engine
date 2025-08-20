/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.model;

import com.top_logic.mig.html.SelectionModel;

/**
 * Listener interfaces that is informed about changes in {@link SelectionModel} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface SelectionListener<T> {

	/**
	 * Notifies the receiver that the selected objects of the given {@link SelectionModel} have
	 * changed.
	 * 
	 * @param model
	 *        the changed {@link SelectionModel}.
	 * @param event
	 *        The event describing the change.
	 */
	void notifySelectionChanged(SelectionModel<T> model, SelectionEvent<T> event);

}