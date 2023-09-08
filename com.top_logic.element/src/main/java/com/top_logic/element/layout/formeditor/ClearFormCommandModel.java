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
import com.top_logic.util.Resources;

/**
 * Clears the display description.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class ClearFormCommandModel extends AbstractCommandModel {

	private FormDefinition _tmp;

	private static final String LABEL = Resources.getInstance().getString(I18NConstants.DELETE_FORM);

	/**
	 * Creates a new {@link ClearFormCommandModel}.
	 * 
	 * @param tmp
	 *        The actual version of the {@link FormDefinition} in the form editor.
	 */
	public ClearFormCommandModel(FormDefinition tmp) {
		_tmp = tmp;

		setLabel(LABEL);
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