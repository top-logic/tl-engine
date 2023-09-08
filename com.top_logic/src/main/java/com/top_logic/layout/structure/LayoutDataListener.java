/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.basic.listener.PropertyListener;

/**
 * {@link PropertyListener} that handles change if the {@link LayoutData} of some object.
 * 
 * @see LayoutModel#LAYOUT_DATA_PROPERTY
 * @see LayoutModel#LAYOUT_SIZE_UPDATE_PROPERTY
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface LayoutDataListener extends PropertyListener {

	/**
	 * Handles change of the {@link LayoutData} of the given object.
	 * 
	 * @param sender
	 *        The object whose data changed.
	 * @param oldData
	 *        The old {@link LayoutData}.
	 * @param newData
	 *        The new {@link LayoutData}.
	 * @param layoutSizeUpdate
	 *        Whether layout data has been changed, due to client triggered size adjustments, or not
	 */
	void handleLayoutDataChanged(Object sender, LayoutData oldData, LayoutData newData, boolean layoutSizeUpdate);

}

