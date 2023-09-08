/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import java.util.Collections;

import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.DirtyHandlingVeto;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.model.FormContext;

/**
 * {@link ColumnChangeVetoListener} which has a veto when the form context of a given
 * {@link FormHandler} is changed. In this case a default dirty handling dialog is displayed.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultColumnChangeVetoListener implements ColumnChangeVetoListener {

	private final FormHandler _handler;

	/**
	 * Creates a new {@link DefaultColumnChangeVetoListener}.
	 * 
	 * @param handler
	 *        the provider of the {@link FormContext} to check
	 */
	public DefaultColumnChangeVetoListener(FormHandler handler) {
		_handler = handler;
	}

	/**
	 * The provider of the {@link FormContext} to check.
	 */
	protected FormHandler getHandler() {
		return _handler;
	}

	@Override
	public void checkVeto(ColumnChangeEvt evt) throws VetoException {
		FormHandler handler = getHandler();
		if (!handler.hasFormContext() || !handler.getFormContext().isChanged()) {
			return;
		} else {
			internalCheck(evt);
		}
	}

	/**
	 * Checks the columns event. The {@link #getHandler() form handler} has either no
	 * {@link FormContext} or it is unchanged.
	 * 
	 * @param evt
	 *        The {@link ColumnChangeEvt}.
	 * @throws DirtyHandlingVeto
	 *         When the event must not be processed.
	 */
	protected void internalCheck(ColumnChangeEvt evt) throws DirtyHandlingVeto {
		throw new DirtyHandlingVeto(Collections.singletonList(getHandler()));
	}
}
