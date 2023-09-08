/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.list;

import java.util.List;

import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import com.top_logic.layout.Drop.DropException;

/**
 * The {@link DropAcceptor} is the one the {@link ListControl} delegates its
 * {@link ListControl#notifyDrop(List, Object)} method to.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface DropAcceptor<T> {

	/**
	 * @param businessObjects
	 *        the T's whose client side representation was moved.
	 * @param listModel
	 *        the model to which the given <code>businessObject</code>s must be dropped
	 * @param selectionModel
	 * 		  the model, which holds indices of the list model, that are selected
	 * @param index
	 *        the index where the client side object was inserted to. must not be &lt; 0 || &gt;
	 *        {@link ListModel#getSize()}.
	 * @return whether the moved objects could be inserted into the given <code>listModel</code> on
	 *         the given position in the correct order (i.e. the order in the list). If
	 *         <code>false</code> the objects are nevertheless inserted into the {@link ListModel}.
	 * 
	 * @throws DropException
	 *         if not all objects could be inserted into the given model
	 */
	boolean accept(List<? extends T> businessObjects, ListModel listModel, ListSelectionModel selectionModel, int index) throws DropException;

}
