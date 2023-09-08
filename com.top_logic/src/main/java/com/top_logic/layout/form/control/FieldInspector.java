/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.util.Map;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.scripting.recorder.gui.inspector.GuiInspectorCommand;
import com.top_logic.layout.scripting.recorder.gui.inspector.GuiInspectorPluginFactory;
import com.top_logic.layout.scripting.recorder.gui.inspector.InspectorModel;

/**
 * {@link GuiInspectorCommand} inspecting {@link AbstractFormFieldControlBase} {@link Control}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FieldInspector extends GuiInspectorCommand<AbstractFormFieldControlBase, FormField> {

	/**
	 * Singleton {@link FieldInspector} instance.
	 */
	public static final FieldInspector INSTANCE = new FieldInspector();

	private FieldInspector() {
		// Singleton constructor.
	}

	@Override
	protected FormField findInspectedGuiElement(AbstractFormFieldControlBase control, Map<String, Object> arguments)
			throws AssertionError {
		return control.getFieldModel();
	}

	@Override
	protected void buildInspector(InspectorModel inspector, FormField model) {
		GuiInspectorPluginFactory.createFieldAssertions(inspector, model);
	}
}
