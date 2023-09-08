/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.templates;

import static com.top_logic.layout.basic.fragments.Fragments.*;

import com.top_logic.layout.Control;
import com.top_logic.layout.basic.FragmentControl;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link ControlProvider} for the parameter group of the template editor.
 * 
 * @see TemplateEditModel#getParameters()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TabularParameterDisplay implements ControlProvider {

	/**
	 * Singleton {@link TabularParameterDisplay} instance.
	 */
	public static final TabularParameterDisplay INSTANCE = new TabularParameterDisplay();

	private TabularParameterDisplay() {
		// Singleton constructor.
	}

	@Override
	public Control createControl(Object model, String style) {
		FormGroup group = (FormGroup) model;
		
		return new FragmentControl(
			table("scriptingRecorderTemplateParams",
				tr(
					th("scriptingRecorderLabelColumn",
						value(group.getMember("sort"))
					),
					th("scriptingRecorderLabelColumn",
						message(I18NConstants.PARAMETER_HEADING)),
					th("scriptingRecorderContentColumn",
						message(I18NConstants.DEFAULT_VALUE_HEADIN))),
				foreach(group.getContainer("content"),
					tbody(loop(tr(
						td(refValue("remove")),

						refScope("item",
							td(
								refValue(TemplateEditModel.Parameter.NAME_ATTRIBUTE),
								refError(TemplateEditModel.Parameter.NAME_ATTRIBUTE)
							),
							td("scriptRecorderContentColumn",
								refValue(TemplateEditModel.Parameter.DEFAULT),
								refError(TemplateEditModel.Parameter.DEFAULT)
							)))))),
				tr(
					th(attributes(attribute(HTMLConstants.COLSPAN_ATTR, "3")),
						value(group.getMember("add"))
					)
				)));
	}

}
