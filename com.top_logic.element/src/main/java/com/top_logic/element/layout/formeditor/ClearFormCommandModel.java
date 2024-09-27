/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Clears the display description.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class ClearFormCommandModel extends AbstractCommandModel {

	private FormDefinition _tmp;

	/**
	 * Creates a new {@link ClearFormCommandModel}.
	 * 
	 * @param tmp
	 *        The actual version of the {@link FormDefinition} in the form editor.
	 */
	public ClearFormCommandModel(FormDefinition tmp) {
		_tmp = tmp;

		setLabel(I18NConstants.DELETE_FORM);
		setImage(Icons.DELETE_FORM);
		setNotExecutableImage(Icons.DELETE_FORM_DISABLED);
	}

	@Override
	protected HandlerResult internalExecuteCommand(DisplayContext context) {
		if (_tmp != null) {
			_tmp.getContent().clear();
		}

		return HandlerResult.DEFAULT_RESULT;
	}
}