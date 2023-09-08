/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import com.top_logic.layout.ChangeCheckVetoListener;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.check.CheckScope;
import com.top_logic.layout.basic.check.SingletonCheckScope;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormHandler;

/**
 * A {@link ValueVetoListener} which will show a confirm dialog like switching to a new tab if some
 * {@link FormHandler} of a given {@link CheckScope} has changes.
 * 
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class CheckChangesValueVetoListener extends ChangeCheckVetoListener implements ValueVetoListener {

    /**
	 * Create a new {@link CheckChangesValueVetoListener}.
	 * 
	 * @param formHandler
	 *        The {@link FormHandler} to check for changes.
	 */
	public CheckChangesValueVetoListener(FormHandler formHandler) {
		this(new SingletonCheckScope(formHandler));
	}

	/**
	 * Creates a new {@link CheckChangesValueVetoListener}.
	 * 
	 * @param checkScope
	 *        The provider for the {@link FormHandler}s to check.
	 */
	public CheckChangesValueVetoListener(CheckScope checkScope) {
		super(checkScope);
    }


    @Override
	public void checkVeto(FormField field, Object newValue) throws VetoException {
		checkVeto();
    }

}
