/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.layout.basic.DirtyHandling;
import com.top_logic.layout.basic.check.CheckScope;
import com.top_logic.layout.form.FormHandler;

/**
 * Abstract {@link VetoListener} that has a veto if some {@link FormHandler} are "dirty".
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ChangeCheckVetoListener implements VetoListener {

	/** Definition of the {@link FormHandler} that may be dirty. */
	protected final CheckScope _checkScope;

	/**
	 * Creates a new {@link ChangeCheckVetoListener}.
	 */
	public ChangeCheckVetoListener(CheckScope checkScope) {
		this._checkScope = checkScope;
	}

	/**
	 * Checks whether the {@link FormHandler} of the given {@link CheckScope} are dirty. In such
	 * dirty-handling is fulfilled with the continuation command of the thrown {@link VetoException}
	 * .
	 */
	protected void checkVeto() throws VetoException {
		DirtyHandling.checkVeto(_checkScope);
	}

}

