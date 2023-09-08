/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.model;

import com.top_logic.layout.SingleSelectionModel;

/**
 * The class {@link SingleSelectionListener} is a listener for {@link SingleSelectionModel}.
 * It will be informed if the selection of the {@link SingleSelectionModel} changed.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface SingleSelectionListener {

	/**
	 * This method will be called if the selected object of the given
	 * {@link SingleSelectionModel} changed.
	 * 
	 * @param model
	 *            the {@link SingleSelectionModel} whose selection changed.
	 * @param formerlySelectedObject
	 *            the object which was selected before the selection changed. may be
	 *            <code>null</code>.
	 * @param selectedObject
	 *            the newly selected object. may be <code>null</code>.
	 */
	public void notifySelectionChanged(SingleSelectionModel model, Object formerlySelectedObject, Object selectedObject);

}
