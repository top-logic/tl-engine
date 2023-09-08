/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.util.Map;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.scripting.recorder.gui.inspector.GuiInspectorCommand;
import com.top_logic.layout.scripting.recorder.gui.inspector.GuiInspectorPluginFactory;
import com.top_logic.layout.scripting.recorder.gui.inspector.InspectorModel;

/**
 * {@link GuiInspectorCommand} inspecting {@link LabelControl}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LabelInspector extends GuiInspectorCommand<LabelControl, FormMember> {

	/**
	 * Singleton {@link LabelInspector} instance.
	 */
	public static final LabelInspector INSTANCE = new LabelInspector();

	private LabelInspector() {
		// Singleton constructor.
	}

	@Override
	protected FormMember findInspectedGuiElement(LabelControl control, Map<String, Object> arguments)
			throws AssertionError {
		return control.getModel();
	}

	@Override
	protected void buildInspector(InspectorModel inspector, FormMember model) {
		if (model instanceof FormField) {
			GuiInspectorPluginFactory.createFieldAssertions(inspector, (FormField) model);
		} else {
			GuiInspectorPluginFactory.createFormMemberAssertions(inspector, model);
		}
	}
}
