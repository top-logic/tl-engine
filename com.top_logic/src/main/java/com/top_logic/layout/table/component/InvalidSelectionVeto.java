/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.component;

import java.util.function.Consumer;

import com.top_logic.layout.VetoException;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.layout.form.treetable.component.SilentVetoException;
import com.top_logic.layout.table.control.SelectionVetoListener;
import com.top_logic.layout.table.control.TableControl.SelectionType;
import com.top_logic.mig.html.SelectionModel;

/**
 * {@link SelectionVetoListener} that rejects a selection of an invalid object.
 * 
 * @see ComponentUtil#isValid(Object)
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class InvalidSelectionVeto implements SelectionVetoListener {

	private Consumer<Object> _showErrorMessage;

	/**
	 * Creates a new {@link InvalidSelectionVeto}.
	 * 
	 * @param errorMessageDisplay
	 *        {@link Consumer} that is called with the invalid object. The consumer should ensure
	 *        that a message about the invalid object is created and displayed to the user.
	 */
	public InvalidSelectionVeto(Consumer<Object> errorMessageDisplay) {
		_showErrorMessage = errorMessageDisplay;
	}

	@Override
	public void checkVeto(SelectionModel selectionModel, Object newSelectedRow, SelectionType selectionType)
			throws VetoException {
		if (!ComponentUtil.isValid(newSelectedRow)) {
			_showErrorMessage.accept(newSelectedRow);
			throw new SilentVetoException();
		}
	}
}
