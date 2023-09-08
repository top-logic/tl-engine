/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree;

import com.top_logic.layout.ChangeCheckVetoListener;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.check.CheckScope;
import com.top_logic.mig.html.SelectionModel;

/**
 * {@link NodeSelectionVetoListener} that checks a given {@link CheckScope} for being dirty.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ChangeCheckNodeSelectionVetoListener extends ChangeCheckVetoListener implements NodeSelectionVetoListener {

	/**
	 * Creates a new {@link ChangeCheckNodeSelectionVetoListener}.
	 */
	public ChangeCheckNodeSelectionVetoListener(CheckScope checkScope) {
		super(checkScope);
	}

	@Override
	public void checkVeto(SelectionModel selectionModel, Object changedSelection, boolean selected)
			throws VetoException {
		checkVeto();
	}

}

