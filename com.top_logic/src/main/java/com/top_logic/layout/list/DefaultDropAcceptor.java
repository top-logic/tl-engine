/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.list;

import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import com.top_logic.layout.Drop.DropException;

/**
 * The class {@link DefaultDropAcceptor} accepts almost all drops.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultDropAcceptor<T> implements DropAcceptor<T> {

	/**
	 * {@link DefaultDropAcceptor} which handles {@link Object}s.
	 */
	public static final DefaultDropAcceptor<Object> OBJECT_INSTANCE = new DefaultDropAcceptor<>();

	/**
	 * Expects that the given {@link ListModel} is a {@link DefaultListModel} to insert the given
	 * <code>businessObject</code> at the given index. if not it throws a {@link DropException}.
	 * 
	 * @see DropAcceptor#accept(List, ListModel, ListSelectionModel, int)
	 */
	@Override
	public boolean accept(List<? extends T> businessObjects, ListModel listModel, ListSelectionModel selectionModel, int index) throws DropException {
		int startIndex = index;
		if (listModel instanceof DefaultListModel) {
			for (T businessObject : businessObjects) {
				((DefaultListModel) listModel).add(index, businessObject);
				index++;
			}
			if (businessObjects.size() > 0) {
				selectionModel.setSelectionInterval(startIndex, startIndex + businessObjects.size() - 1);
			}
			return true;
		}
		throw new DropException("not drop to listmodel of type '" + listModel.getClass().getName() + "'");
	}
}
