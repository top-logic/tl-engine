/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.selection;


import com.top_logic.layout.ChangeCheckVetoListener;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.check.CheckScope;

/**
 * {@link DefaultSingleSelectVetoListener} is the default implementation for
 * veto listener for {@link SingleSelectionModel}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultSingleSelectVetoListener extends ChangeCheckVetoListener implements SingleSelectVetoListener {

	/**
	 * Creates a new {@link DefaultSingleSelectVetoListener}.
	 * @param aCheckScope
	 *        the scope to check whether changes exists
	 */
	public DefaultSingleSelectVetoListener(CheckScope aCheckScope) {
		super(aCheckScope);
	}

	@Override
	public void checkVeto(final SingleSelectionModel aModel, final Object aNode, boolean programmaticUpdate) throws VetoException {
		if (programmaticUpdate) {
			return;
		}
		checkVeto();
	}

}
