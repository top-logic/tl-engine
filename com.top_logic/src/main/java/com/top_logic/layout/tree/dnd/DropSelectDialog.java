/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.dnd;

import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.layout.Drop;
import com.top_logic.layout.tree.TreeControl;
import com.top_logic.mig.html.ReferencedSelectionModel;
import com.top_logic.mig.html.SelectionModel;

/**
 * {@link Drop} for trees in popup select dialogs.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class DropSelectDialog implements Drop<List<?>> {

	private TreeControl _treeControl;

	/**
	 * Create a new {@link DropSelectDialog}.
	 */
	public DropSelectDialog(TreeControl treeControl) {
		_treeControl = treeControl;
	}

	@Override
	public String getID() {
		return _treeControl.getID();
	}

	@Override
	public void notifyDrop(List<?> value, Object dropInfo) throws DropException {
		SelectionModel selectionModel = _treeControl.getSelectionModel();
		if (selectionModel instanceof ReferencedSelectionModel) {
			((ReferencedSelectionModel) selectionModel).propagateReferencedSelectionChange(CollectionUtil.toSet(value));
		} else {
			_treeControl.requestRepaint();
		}
	}

}
