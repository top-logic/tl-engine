/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.dnd;

import java.util.List;

import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import com.top_logic.layout.Drop.DropException;
import com.top_logic.layout.list.DropAcceptor;
import com.top_logic.layout.list.ListControl;

/**
 * {@link DropAcceptor} for {@link ListControl}s for drag and drop operations, started at trees.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class DropFromTreeAcceptor implements DropAcceptor<Object> {

	private DropAcceptor<Object> _listDropAcceptor;

	/**
	 * Create a new {@link DropFromTreeAcceptor}.
	 */
	public DropFromTreeAcceptor(DropAcceptor<Object> listDropAcceptor) {
		_listDropAcceptor = listDropAcceptor;
	}

	@Override
	public boolean accept(List<? extends Object> businessObjects, ListModel listModel,
			ListSelectionModel selectionModel, int index) throws DropException {
		_listDropAcceptor.accept(businessObjects, listModel, selectionModel, index);
		return false;
	}

}
