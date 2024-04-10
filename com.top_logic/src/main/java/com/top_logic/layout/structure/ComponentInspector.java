/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.util.Map;

import com.top_logic.layout.scripting.recorder.gui.inspector.GuiInspectorCommand;
import com.top_logic.layout.scripting.recorder.gui.inspector.GuiInspectorPluginFactory;
import com.top_logic.layout.scripting.recorder.gui.inspector.InspectorModel;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link GuiInspectorCommand} for {@link ContentControl}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ComponentInspector extends GuiInspectorCommand<ContentControl, LayoutComponent> {

	/**
	 * Singleton {@link ComponentInspector} instance.
	 */
	public static final ComponentInspector INSTANCE = new ComponentInspector();

	private ComponentInspector() {
		// Singleton constructor.
	}

	@Override
	protected LayoutComponent findInspectedGuiElement(ContentControl control, Map<String, Object> arguments)
			throws AssertionError {
		return control.getModel();
	}

	@Override
	protected void buildInspector(InspectorModel inspector, LayoutComponent model) {
		GuiInspectorPluginFactory.createComponentInformation(inspector, model);
	}
}
