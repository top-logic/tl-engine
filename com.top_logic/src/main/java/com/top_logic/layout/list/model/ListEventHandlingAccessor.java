/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.list.model;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;

/**
 * Interface for allowing controlled access to non-public methods for event
 * dispatching in {@link ListModel} implementations based on e.g.
 * {@link DefaultListModel}.
 * 
 * <p>
 * Service methods in {@link ListModelUtilities} rely on the calling
 * {@link ListModel} implementation to provide an accessor implementation of
 * this {@link ListEventHandlingAccessor} interface.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ListEventHandlingAccessor {

	/**
	 * @see AbstractListModel#fireContentsChanged(Object, int, int)
	 */
	public void fireContentsChanged(Object sender, int index0, int index1);

	/**
	 * @see AbstractListModel#fireIntervalAdded(Object, int, int)
	 */
	public void fireIntervalAdded(Object sender, int index0, int index1);

	/**
	 * @see AbstractListModel#fireIntervalRemoved(Object, int, int)
	 */
	public void fireIntervalRemoved(Object sender, int index0, int index1);

}
