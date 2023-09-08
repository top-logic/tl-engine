/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.util.Map;

import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.scripting.recorder.gui.inspector.GuiInspectorCommand;
import com.top_logic.layout.scripting.recorder.gui.inspector.GuiInspectorPluginFactory;
import com.top_logic.layout.scripting.recorder.gui.inspector.InspectorModel;

/**
 * {@link GuiInspectorCommand} inspecting {@link ChecklistControl}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ChecklistInspector extends GuiInspectorCommand<ChecklistControl, SelectField> {

	/**
	 * Singleton {@link ChecklistInspector} instance.
	 */
	public static final ChecklistInspector INSTANCE = new ChecklistInspector();

	private ChecklistInspector() {
		// Singleton constructor.
	}

	@Override
	protected SelectField findInspectedGuiElement(ChecklistControl control, Map<String, Object> arguments)
			throws AssertionError {
		return control.getSelectField();
	}

	@Override
	protected void buildInspector(InspectorModel inspector, SelectField model) {
		GuiInspectorPluginFactory.createFieldAssertions(inspector, model);
	}
}
