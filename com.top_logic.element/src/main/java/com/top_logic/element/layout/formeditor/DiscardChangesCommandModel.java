/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Sets the display description of a given model to the last persistent version of it. So all
 * temporary changes are discarded.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class DiscardChangesCommandModel extends AbstractCommandModel {

	private FormDefinition _origin;

	private FormDefinition _tmp;

	/**
	 * 
	 * Creates a {@link DiscardChangesCommandModel}.
	 * 
	 * @param origin
	 *        The last persistent version of the {@link FormDefinition}.
	 * @param tmp
	 *        The actual version of the {@link FormDefinition} in the form editor.
	 */
	public DiscardChangesCommandModel(FormDefinition origin, FormDefinition tmp) {
		_origin = origin;
		_tmp = tmp;

		setLabel(I18NConstants.DISCARD_CHANGES);
		setImage(Icons.DISCARD_CHANGES);
		setNotExecutableImage(Icons.DISCARD_CHANGES_DISABLED);
	}

	private FormDefinition getFormDefintionOrigin() {
		return _origin;
	}

	private FormDefinition getFormDefintionTmp() {
		return _tmp;
	}

	@Override
	protected HandlerResult internalExecuteCommand(DisplayContext context) {
		getFormDefintionTmp().setContent(getOriginContent());

		return HandlerResult.DEFAULT_RESULT;
	}

	private List<PolymorphicConfiguration<? extends FormElementTemplateProvider>> getOriginContent() {
		List<PolymorphicConfiguration<? extends FormElementTemplateProvider>> content = Collections.emptyList();
		if (getFormDefintionOrigin() != null) {
			content = new ArrayList<>(getFormDefintionOrigin().getContent());
		}
		return content;
	}
}